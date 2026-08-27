package seed.seedplusbackend.commercial.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import seed.seedplusbackend.commercial.application.port.SeoulCommercialEstimatedSalesClientPort;
import seed.seedplusbackend.commercial.application.result.CommercialEstimatedSalesPageResult;
import seed.seedplusbackend.commercial.application.result.CommercialEstimatedSalesRowResult;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;

@ExtendWith(MockitoExtension.class)
@DisplayName("최신 추정매출 분기 조회기")
class LatestEstimatedSalesQuarterResolverTest {

  @Mock private SeoulCommercialEstimatedSalesClientPort clientPort;

  @Test
  @DisplayName("현재 분기에 데이터가 없으면 직전 데이터 보유 분기를 반환한다")
  void resolvesLatestAvailableQuarter() {
    CommercialEstimatedSalesPageResult availablePage = page("20262");
    given(clientPort.fetchByQuarter("20263", 1, 1)).willReturn(emptyPage());
    given(clientPort.fetchByQuarter("20262", 1, 1)).willReturn(availablePage);

    String result = resolver().resolve(LocalDate.of(2026, 8, 27));

    assertThat(result).isEqualTo("20262");
  }

  @Test
  @DisplayName("현재 분기에 데이터가 있으면 이전 분기는 조회하지 않는다")
  void stopsAtFirstAvailableQuarter() {
    CommercialEstimatedSalesPageResult availablePage = page("20261");
    given(clientPort.fetchByQuarter("20261", 1, 1)).willReturn(availablePage);

    String result = resolver().resolve(LocalDate.of(2026, 2, 1));

    assertThat(result).isEqualTo("20261");
    verify(clientPort, never()).fetchByQuarter("20254", 1, 1);
  }

  @Test
  @DisplayName("전체 건수와 조회 행이 모순되면 응답 형식 오류로 처리한다")
  void rejectsInconsistentPage() {
    given(clientPort.fetchByQuarter("20263", 1, 1))
        .willReturn(new CommercialEstimatedSalesPageResult(1, List.of()));

    assertThatThrownBy(() -> resolver().resolve(LocalDate.of(2026, 8, 27)))
        .isInstanceOf(ApplicationException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.SEOUL_OPEN_API_INVALID_RESPONSE);
  }

  @Test
  @DisplayName("최근 8개 분기에 데이터가 없으면 명확하게 실패한다")
  void throwsWhenNoQuarterHasData() {
    String[] quarters = {"20263", "20262", "20261", "20254", "20253", "20252", "20251", "20244"};
    for (String quarter : quarters) {
      given(clientPort.fetchByQuarter(quarter, 1, 1)).willReturn(emptyPage());
    }

    assertThatThrownBy(() -> resolver().resolve(LocalDate.of(2026, 8, 27)))
        .isInstanceOf(ApplicationException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.SEOUL_ESTIMATED_SALES_QUARTER_NOT_FOUND);
  }

  @Test
  @DisplayName("API 요청 실패를 과거 분기 데이터로 숨기지 않는다")
  void propagatesApiFailure() {
    ApplicationException exception =
        new ApplicationException(ErrorCode.SEOUL_OPEN_API_REQUEST_FAILED);
    given(clientPort.fetchByQuarter("20263", 1, 1)).willThrow(exception);

    assertThatThrownBy(() -> resolver().resolve(LocalDate.of(2026, 8, 27))).isSameAs(exception);
    verify(clientPort, never()).fetchByQuarter("20262", 1, 1);
  }

  private LatestEstimatedSalesQuarterResolver resolver() {
    return new LatestEstimatedSalesQuarterResolver(clientPort);
  }

  private CommercialEstimatedSalesPageResult emptyPage() {
    return new CommercialEstimatedSalesPageResult(0, List.of());
  }

  private CommercialEstimatedSalesPageResult page(String quarter) {
    CommercialEstimatedSalesRowResult row = mock(CommercialEstimatedSalesRowResult.class);
    given(row.stdrYyquCd()).willReturn(quarter);
    return new CommercialEstimatedSalesPageResult(1, List.of(row));
  }
}
