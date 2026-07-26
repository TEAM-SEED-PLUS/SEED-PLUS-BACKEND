package seed.seedplusbackend.commercial.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import seed.seedplusbackend.commercial.application.port.CommercialDataCollectClaimPort;
import seed.seedplusbackend.support.AbstractPostgresContainerTest;
import seed.seedplusbackend.support.RepositoryTest;

@RepositoryTest
@Import(CommercialDataCollectClaimJdbcAdapter.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DisplayName("CommercialDataCollectClaimJdbcAdapter")
class CommercialDataCollectClaimJdbcAdapterTest extends AbstractPostgresContainerTest {

  @Autowired private CommercialDataCollectClaimPort claimPort;
  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  @DisplayName("동일한 수집 작업을 동시에 요청해도 하나만 실행권을 얻는다")
  void tryClaim_allowsOnlyOneConcurrentRequest() throws Exception {
    String targetKey = "concurrent-" + UUID.randomUUID();
    CyclicBarrier barrier = new CyclicBarrier(2);
    Callable<Optional<Long>> claimTask =
        () -> {
          barrier.await(5, TimeUnit.SECONDS);
          return claimPort.tryClaim("SMALL_BUSINESS_STORE", targetKey, false);
        };
    ExecutorService executor = Executors.newFixedThreadPool(2);

    try {
      List<Optional<Long>> results =
          executor.invokeAll(List.of(claimTask, claimTask)).stream()
              .map(
                  future -> {
                    try {
                      return future.get(5, TimeUnit.SECONDS);
                    } catch (Exception exception) {
                      throw new AssertionError(exception);
                    }
                  })
              .toList();

      assertThat(results).filteredOn(Optional::isPresent).hasSize(1);
      assertThat(runningCount("SMALL_BUSINESS_STORE", targetKey)).isEqualTo(1);
    } finally {
      executor.shutdownNow();
    }
  }

  @Test
  @DisplayName("완료된 작업은 일반 요청을 막고 force 요청에만 실행권을 준다")
  void tryClaim_restartsCompletedTargetOnlyWhenForced() {
    String targetKey = "completed-" + UUID.randomUUID();
    Long historyId = claimPort.tryClaim("SMALL_BUSINESS_STORE", targetKey, false).orElseThrow();
    jdbcTemplate.update(
        """
        UPDATE commercial_data_collect_histories
        SET status = 'COMPLETED', finished_at = now(), updated_at = now()
        WHERE commercial_data_collect_history_id = ?
        """,
        historyId);

    assertThat(claimPort.tryClaim("SMALL_BUSINESS_STORE", targetKey, false)).isEmpty();
    assertThat(claimPort.tryClaim("SMALL_BUSINESS_STORE", targetKey, true)).contains(historyId);
    assertThat(runningCount("SMALL_BUSINESS_STORE", targetKey)).isEqualTo(1);
  }

  private Integer runningCount(String dataType, String targetKey) {
    return jdbcTemplate.queryForObject(
        """
        SELECT COUNT(*)
        FROM commercial_data_collect_histories
        WHERE data_type = ? AND target_key = ? AND status = 'RUNNING'
        """,
        Integer.class,
        dataType,
        targetKey);
  }
}
