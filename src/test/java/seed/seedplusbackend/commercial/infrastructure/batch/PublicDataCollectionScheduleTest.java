package seed.seedplusbackend.commercial.infrastructure.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import seed.seedplusbackend.commercial.application.provider.CommercialDataType;

@DisplayName("공공데이터 정기 수집 일정")
class PublicDataCollectionScheduleTest {

  @Test
  @DisplayName("서울 시간 기준으로 데이터별 기본 수집 주기를 적용한다")
  void applicationDefaultsUseDifferentFrequenciesInSeoulTime() {
    new ApplicationContextRunner()
        .withUserConfiguration(Config.class)
        .withInitializer(
            context -> {
              try {
                new YamlPropertySourceLoader()
                    .load("application", new ClassPathResource("application.yml"))
                    .forEach(context.getEnvironment().getPropertySources()::addLast);
              } catch (java.io.IOException exception) {
                throw new IllegalStateException(exception);
              }
            })
        .run(
            context -> {
              assertThat(context).hasNotFailed();
              var properties = context.getBean(PublicDataCollectionProperties.class);
              var schedule = new PublicDataCollectionSchedule(properties);
              Instant after = Instant.parse("2026-09-11T00:30:00Z");
              assertThat(schedule.nextExecution(CommercialDataType.SEOUL_SDOT_FOOT_TRAFFIC, after))
                  .contains(Instant.parse("2026-09-11T01:00:00Z"));
              assertThat(schedule.nextExecution(CommercialDataType.SMALL_BUSINESS_STORE, after))
                  .contains(Instant.parse("2026-09-12T19:00:00Z"));
              assertThat(schedule.nextExecution(CommercialDataType.KOSIS_BUSINESS_COUNT, after))
                  .contains(Instant.parse("2026-09-13T20:00:00Z"));
              assertThat(schedule.nextExecution(CommercialDataType.REB_SMALL_RETAIL_RENT, after))
                  .isEmpty();
              assertThat(properties.storeSigunguCodes()).isEmpty();
              assertThat(properties.cityAreas()).isEmpty();
            });
  }

  @Test
  @DisplayName("실시간 수집 주기를 10분으로 변경하거나 비활성화할 수 있다")
  void canChangeRealtimeIntervalToTenMinutesOrDisableIt() {
    var properties =
        new PublicDataCollectionProperties(
            ZoneId.of("Asia/Seoul"),
            Map.of(
                CommercialDataType.SEOUL_SDOT_FOOT_TRAFFIC, "0 */10 * * * *",
                CommercialDataType.SEOUL_REALTIME_CITY_POPULATION, "-"),
            List.of(),
            List.of());
    var schedule = new PublicDataCollectionSchedule(properties);
    Instant after = Instant.parse("2026-09-11T00:31:00Z");
    assertThat(schedule.nextExecution(CommercialDataType.SEOUL_SDOT_FOOT_TRAFFIC, after))
        .contains(Instant.parse("2026-09-11T00:40:00Z"));
    assertThat(schedule.nextExecution(CommercialDataType.SEOUL_REALTIME_CITY_POPULATION, after))
        .isEmpty();
  }

  @Test
  @DisplayName("잘못된 Cron 표현식과 임대료 CSV 자동 수집 설정을 거부한다")
  void rejectsInvalidCronAndManualCsvSchedule() {
    assertThatThrownBy(
            () ->
                new PublicDataCollectionProperties(
                    ZoneId.of("Asia/Seoul"),
                    Map.of(CommercialDataType.SEOUL_SDOT_FOOT_TRAFFIC, "invalid"),
                    null,
                    null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () ->
                new PublicDataCollectionProperties(
                    ZoneId.of("Asia/Seoul"),
                    Map.of(CommercialDataType.REB_SMALL_RETAIL_RENT, "0 0 * * * *"),
                    null,
                    null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Configuration(proxyBeanMethods = false)
  @EnableConfigurationProperties(PublicDataCollectionProperties.class)
  static class Config {}
}
