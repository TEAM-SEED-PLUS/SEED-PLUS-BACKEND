package seed.seedplusbackend.commercial.application.command;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Year;
import java.time.ZoneId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("KOSIS 신생기업 생존율 수집 명령")
class KosisBusinessSurvivalCollectCommandTest {

  private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Seoul");

  @Test
  @DisplayName("최신 연도 조회 키에는 서울 기준 현재 연도를 포함한다")
  void targetKey_includesCurrentYearForLatestSearch() {
    KosisBusinessSurvivalCollectCommand command =
        new KosisBusinessSurvivalCollectCommand(null, null, 3, false);

    assertThat(command.targetKey()).isEqualTo("LATEST:3:" + Year.now(BUSINESS_ZONE).getValue());
  }

  @Test
  @DisplayName("기간 조회 키에는 요청한 시작 연도와 종료 연도를 사용한다")
  void targetKey_usesRequestedRangeForPeriodSearch() {
    KosisBusinessSurvivalCollectCommand command =
        new KosisBusinessSurvivalCollectCommand(2021, 2023, null, false);

    assertThat(command.targetKey()).isEqualTo("PERIOD:2021-2023");
  }
}
