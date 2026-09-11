package seed.seedplusbackend.commercial.infrastructure.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import seed.seedplusbackend.commercial.application.command.CommercialDataCollectCommand;
import seed.seedplusbackend.commercial.application.command.SeoulSdotFootTrafficCollectCommand;
import seed.seedplusbackend.commercial.application.port.SeoulSdotFootTrafficStorePort;
import seed.seedplusbackend.commercial.application.provider.*;
import seed.seedplusbackend.commercial.application.result.SeoulSdotFootTrafficRowResult;
import seed.seedplusbackend.commercial.infrastructure.repository.*;
import seed.seedplusbackend.support.AbstractPostgresContainerTest;

@SpringJUnitConfig(PublicDataBatchIntegrationTest.Config.class)
@TestPropertySource(properties = "public-data.batch.enabled=true")
@DisplayName("공공데이터 배치 실행과 원자적 반영")
class PublicDataBatchIntegrationTest extends AbstractPostgresContainerTest {

  private static final CommercialDataType TYPE = CommercialDataType.SEOUL_SDOT_FOOT_TRAFFIC;
  private static final LocalDateTime TIME = LocalDateTime.of(2026, 9, 11, 10, 0);

  @Autowired private PublicDataBatchLauncher launcher;
  @Autowired private PublicDataBatchLease lease;
  @Autowired private PublicDataBatchCommandFactory factory;
  @Autowired private CommercialDataProviderRegistry registry;
  @Autowired private SeoulSdotFootTrafficStorePort store;
  @Autowired private JdbcTemplate jdbc;
  @Autowired private JobRepository jobs;
  @Autowired private JobExplorer explorer;
  private String sensor;

  @BeforeEach
  void prepare() {
    sensor = UUID.randomUUID().toString();
    reset(factory, registry);
    jdbc.update("DELETE FROM public_data_batch_leases WHERE data_type = ?", TYPE.name());
    when(factory.create(TYPE)).thenReturn(List.of(new SeoulSdotFootTrafficCollectCommand(true)));
    store.upsertAll(List.of(row(10, TIME)));
  }

  @Test
  @DisplayName("수집 중 기존 값이 유지되고 전체 완료 후 새 값과 실행 이력이 반영된다")
  void publishesOnlyAfterCompleteCollection() throws Exception {
    provider(
        progress -> {
          store.upsertAll(List.of(row(20, TIME)));
          progress.update(2, 1, 1);
          assertThat(visitorCount()).isEqualTo(10);
          store.upsertAll(List.of(row(30, TIME.plusMinutes(10))));
          progress.update(2, 2, 2);
          assertThat(visitorCount()).isEqualTo(10);
        });
    JobExecution result = launcher.launch(TYPE, slot()).orElseThrow();
    assertThat(result.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    assertThat(visitorCount()).isEqualTo(20);
    assertThat(result.getStepExecutions()).hasSize(2);
    assertThat(
            jdbc.queryForObject(
                "SELECT job_execution_id FROM public_data_batch_publications WHERE data_type = ?",
                Long.class,
                TYPE.name()))
        .isEqualTo(result.getId());
    assertThat(pageCount(result)).isZero();
  }

  @Test
  @DisplayName("페이지 수집 실패 시 기존 값이 유지되고 같은 슬롯으로 재시도할 수 있다")
  void retriesFailedSlotWithoutPublishingPartialRows() throws Exception {
    Instant slot = slot();
    provider(
        progress -> {
          store.upsertAll(List.of(row(20, TIME)));
          progress.update(2, 1, 1);
          throw new IllegalStateException("두 번째 페이지 API 오류");
        });
    JobExecution failed = launcher.launch(TYPE, slot).orElseThrow();
    assertThat(failed.getStatus()).isEqualTo(BatchStatus.FAILED);
    assertThat(visitorCount()).isEqualTo(10);
    assertThat(pageCount(failed)).isZero();
    provider(
        progress -> {
          store.upsertAll(List.of(row(40, TIME)));
          progress.update(1, 1, 1);
        });
    JobExecution retried = launcher.launch(TYPE, slot).orElseThrow();
    assertThat(retried.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    assertThat(retried.getJobInstance().getId()).isEqualTo(failed.getJobInstance().getId());
    assertThat(retried.getId()).isNotEqualTo(failed.getId());
    assertThat(visitorCount()).isEqualTo(40);
  }

  @Test
  @DisplayName("최종 반영 도중 DB 오류가 나도 앞서 갱신한 행까지 롤백한다")
  void rollsBackFailedPublication() throws Exception {
    provider(
        progress -> {
          store.upsertAll(List.of(row(20, TIME)));
          progress.update(2, 1, 1);
          store.upsertAll(List.of(row(-1, TIME.plusMinutes(10))));
          progress.update(2, 2, 2);
        });
    JobExecution result = launcher.launch(TYPE, slot()).orElseThrow();
    assertThat(result.getStatus()).isEqualTo(BatchStatus.FAILED);
    assertThat(visitorCount()).isEqualTo(10);
    assertThat(pageCount(result)).isZero();
  }

  @Test
  @DisplayName("이미 완료한 슬롯은 다시 수집하지 않는다")
  void skipsCompletedSlot() throws Exception {
    AtomicInteger calls = new AtomicInteger();
    provider(
        progress -> {
          calls.incrementAndGet();
          store.upsertAll(List.of(row(20, TIME)));
          progress.update(1, 1, 1);
        });
    Instant slot = slot();
    JobExecution first = launcher.launch(TYPE, slot).orElseThrow();
    JobExecution second = launcher.launch(TYPE, slot).orElseThrow();
    assertThat(first.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    assertThat(second.getId()).isEqualTo(first.getId());
    assertThat(calls.get()).isEqualTo(1);
  }

  @Test
  @DisplayName("다른 실행이 권한을 갖고 있으면 슬롯이 달라도 중복 실행하지 않는다")
  void skipsWhileAnotherWorkerOwnsSource() throws Exception {
    UUID owner = UUID.randomUUID();
    assertThat(lease.acquire(TYPE, owner)).isTrue();
    assertThat(launcher.launch(TYPE, slot())).isEmpty();
    verifyNoInteractions(registry);
    lease.release(TYPE, owner);
  }

  @Test
  @DisplayName("실행 권한을 잃은 작업은 데이터를 반영하거나 새 작업 권한을 해제하지 못한다")
  void fencesOldWorker() throws Exception {
    UUID newOwner = UUID.randomUUID();
    provider(
        progress -> {
          store.upsertAll(List.of(row(20, TIME)));
          jdbc.update(
              "UPDATE public_data_batch_leases SET owner_token = ? WHERE data_type = ?",
              newOwner,
              TYPE.name());
          progress.update(1, 1, 1);
        });
    JobExecution result = launcher.launch(TYPE, slot()).orElseThrow();
    assertThat(result.getStatus()).isEqualTo(BatchStatus.FAILED);
    assertThat(visitorCount()).isEqualTo(10);
    assertThat(
            jdbc.queryForObject(
                "SELECT owner_token FROM public_data_batch_leases WHERE data_type = ?",
                UUID.class,
                TYPE.name()))
        .isEqualTo(newOwner);
  }

  @Test
  @DisplayName("실행 권한이 만료된 RUNNING 이력을 실패로 복구하고 새 수집을 실행한다")
  void recoversAbandonedExecution() throws Exception {
    var parameters =
        new JobParametersBuilder()
            .addString("source", TYPE.name())
            .addString("slot", slot().toString())
            .addString("ownerToken", UUID.randomUUID().toString(), false)
            .toJobParameters();
    JobExecution stale = jobs.createJobExecution(PublicDataBatchConfiguration.JOB_NAME, parameters);
    stale.setStatus(BatchStatus.STARTED);
    jobs.update(stale);
    StepExecution staleStep = stale.createStepExecution("collectPublicData");
    staleStep.setStatus(BatchStatus.STARTED);
    jobs.add(staleStep);
    UUID oldOwner = UUID.randomUUID();
    lease.acquire(TYPE, oldOwner);
    jdbc.update(
        "UPDATE public_data_batch_leases SET lease_until = clock_timestamp() - interval '1 minute' WHERE data_type = ?",
        TYPE.name());
    provider(
        progress -> {
          store.upsertAll(List.of(row(20, TIME)));
          progress.update(1, 1, 1);
        });
    JobExecution result = launcher.launch(TYPE, slot()).orElseThrow();
    assertThat(result.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    JobExecution recovered = explorer.getJobExecution(stale.getId());
    assertThat(recovered.getStatus()).isEqualTo(BatchStatus.FAILED);
    assertThat(recovered.getStepExecutions())
        .allMatch(step -> step.getStatus() == BatchStatus.FAILED);
  }

  private void provider(Consumer<CollectProgress> action) {
    when(registry.get(TYPE))
        .thenReturn(
            new CommercialDataProvider() {
              public CommercialDataType supports() {
                return TYPE;
              }

              public void collect(CommercialDataCollectCommand command, CollectProgress progress) {
                action.accept(progress);
              }
            });
  }

  private SeoulSdotFootTrafficRowResult row(long count, LocalDateTime time) {
    return new SeoulSdotFootTrafficRowResult(
        "batch-test", sensor, time, null, "강남구", "역삼동", count, null);
  }

  private long visitorCount() {
    return jdbc.queryForObject(
        "SELECT visitor_count FROM seoul_sdot_foot_traffic WHERE serial_number = ? AND sensing_time = ?",
        Long.class,
        sensor,
        TIME);
  }

  private long pageCount(JobExecution execution) {
    return jdbc.queryForObject(
        "SELECT count(*) FROM public_data_batch_pages WHERE job_execution_id = ?",
        Long.class,
        execution.getId());
  }

  private Instant slot() {
    return Instant.ofEpochSecond(
        Math.abs(UUID.randomUUID().getMostSignificantBits() % 1_000_000_000L) * 60);
  }

  @Configuration(proxyBeanMethods = false)
  @EnableBatchProcessing
  @EnableTransactionManagement(proxyTargetClass = true)
  @Import({
    PublicDataBatchConfiguration.class,
    CommercialEstimatedSalesJdbcRepository.class,
    SmallBusinessStoreJdbcRepository.class,
    KosisBusinessCountJdbcRepository.class,
    KosisBusinessSurvivalJdbcRepository.class,
    SeoulSdotFootTrafficJdbcRepository.class,
    SeoulRealtimeCityPopulationJdbcRepository.class
  })
  static class Config {
    @Bean
    DataSource dataSource() {
      return new DriverManagerDataSource(
          POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    }

    @Bean(initMethod = "migrate")
    Flyway flyway(DataSource dataSource) {
      return Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").load();
    }

    @Bean
    @DependsOn("flyway")
    JdbcTemplate jdbcTemplate(DataSource dataSource) {
      return new JdbcTemplate(dataSource);
    }

    @Bean
    PlatformTransactionManager transactionManager(DataSource dataSource) {
      return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    ObjectMapper objectMapper() {
      return Jackson2ObjectMapperBuilder.json().build();
    }

    @Bean
    PublicDataBatchCommandFactory commandFactory() {
      return mock(PublicDataBatchCommandFactory.class);
    }

    @Bean
    CommercialDataProviderRegistry providers() {
      return mock(CommercialDataProviderRegistry.class);
    }
  }
}
