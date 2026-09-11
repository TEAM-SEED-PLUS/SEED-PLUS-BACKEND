package seed.seedplusbackend.analysis.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import seed.seedplusbackend.commercial.application.command.SeoulSdotFootTrafficCollectCommand;
import seed.seedplusbackend.commercial.domain.entity.CommercialDataCollectHistory;
import seed.seedplusbackend.commercial.domain.entity.CommercialDataCollectStatus;
import seed.seedplusbackend.commercial.domain.repository.CommercialDataCollectHistoryRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("공공데이터 수집 완료 대기")
class CommercialDataCollectionWaiterTest {

  @Mock private CommercialDataCollectHistoryRepository historyRepository;

  @Test
  @DisplayName("대기 제한을 초과한 RUNNING 이력을 실패 처리한다")
  void awaitCompletion_failsRunningHistoryAfterTimeout() {
    SeoulSdotFootTrafficCollectCommand command = new SeoulSdotFootTrafficCollectCommand(true);
    CommercialDataCollectHistory history =
        CommercialDataCollectHistory.start(command.dataType().historyKey(), command.targetKey());
    given(
            historyRepository.findTopByDataTypeAndTargetKeyOrderByStartedAtDesc(
                command.dataType().historyKey(), command.targetKey()))
        .willReturn(Optional.of(history));
    CommercialDataCollectionWaiter waiter =
        new CommercialDataCollectionWaiter(historyRepository, Duration.ZERO, Duration.ofMillis(1));

    assertThatThrownBy(() -> waiter.awaitCompletion(command))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("중단 처리");

    assertThat(history.getStatus()).isEqualTo(CommercialDataCollectStatus.FAILED);
    assertThat(history.getFinishedAt()).isNotNull();
    assertThat(history.getErrorMessage()).contains("제한 시간");
    verify(historyRepository).save(history);
  }
}
