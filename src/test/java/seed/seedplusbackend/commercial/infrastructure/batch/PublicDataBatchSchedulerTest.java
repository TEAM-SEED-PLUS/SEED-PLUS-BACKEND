package seed.seedplusbackend.commercial.infrastructure.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.SimpleTriggerContext;
import seed.seedplusbackend.commercial.application.provider.CommercialDataType;

@DisplayName("공공데이터 Cron 스케줄러")
class PublicDataBatchSchedulerTest {
  private static final CommercialDataType TYPE = CommercialDataType.SEOUL_SDOT_FOOT_TRAFFIC;
  private final Clock clock = Clock.fixed(Instant.parse("2026-09-15T00:30:00Z"), ZoneOffset.UTC);
  private final PublicDataBatchLauncher launcher = mock(PublicDataBatchLauncher.class);

  private ScheduledTaskRegistrar register(Map<CommercialDataType, String> crons) {
    var properties =
        new PublicDataCollectionProperties(ZoneId.of("Asia/Seoul"), crons, List.of(), List.of());
    var registrar = new ScheduledTaskRegistrar();
    new PublicDataBatchScheduler(properties, launcher, clock).configureTasks(registrar);
    return registrar;
  }

  @Test
  @DisplayName("서버 시계가 UTC여도 서울 시간 오전 10시에 실행하고 등록 시에는 수집하지 않는다")
  void usesConfiguredZoneWithoutImmediateLaunch() {
    var registrar = register(Map.of(TYPE, "0 0 10 * * *"));
    var task = registrar.getTriggerTaskList().getFirst();
    assertThat(task.getTrigger().nextExecution(new SimpleTriggerContext(clock)))
        .isEqualTo(Instant.parse("2026-09-15T01:00:00Z"));
    verifyNoInteractions(launcher);
  }

  @Test
  @DisplayName("비활성화한 데이터는 예약하지 않고 활성 데이터만 해당 유형으로 실행한다")
  void registersOnlyEnabledSources() throws Exception {
    var registrar =
        register(Map.of(TYPE, "0 * * * * *", CommercialDataType.SEOUL_ESTIMATED_SALES, "-"));
    assertThat(registrar.getTriggerTaskList()).hasSize(1);
    registrar.getTriggerTaskList().getFirst().getRunnable().run();
    verify(launcher).launch(TYPE, clock.instant());
    verifyNoMoreInteractions(launcher);
  }

  @Test
  @DisplayName("실행 예외가 발생해도 다음 예약 호출을 수행할 수 있다")
  void failureDoesNotEscapeScheduledTask() throws Exception {
    when(launcher.launch(TYPE, clock.instant())).thenThrow(new IllegalStateException("테스트 오류"));
    var task = register(Map.of(TYPE, "0 * * * * *")).getTriggerTaskList().getFirst();
    assertThatCode(
            () -> {
              task.getRunnable().run();
              task.getRunnable().run();
            })
        .doesNotThrowAnyException();
    verify(launcher, times(2)).launch(TYPE, clock.instant());
  }

  @Test
  @DisplayName("배치가 비활성화되어 있으면 스케줄러와 실행기를 생성하지 않는다")
  void disabledConfigurationDoesNotRequireBatchInfrastructure() {
    new ApplicationContextRunner()
        .withUserConfiguration(PublicDataBatchConfiguration.class)
        .withPropertyValues("public-data.batch.enabled=false")
        .run(
            context -> {
              assertThat(context).hasNotFailed();
              assertThat(context).doesNotHaveBean(PublicDataBatchScheduler.class);
              assertThat(context).doesNotHaveBean(PublicDataBatchLauncher.class);
            });
  }
}
