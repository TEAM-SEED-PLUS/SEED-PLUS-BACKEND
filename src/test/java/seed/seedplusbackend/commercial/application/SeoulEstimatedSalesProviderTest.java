package seed.seedplusbackend.commercial.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import seed.seedplusbackend.commercial.application.command.CommercialEstimatedSalesCollectCommand;
import seed.seedplusbackend.commercial.application.port.CommercialEstimatedSalesStorePort;
import seed.seedplusbackend.commercial.application.port.SeoulCommercialEstimatedSalesClientPort;
import seed.seedplusbackend.commercial.application.provider.ExternalApiRetryExecutor;
import seed.seedplusbackend.commercial.application.provider.SeoulEstimatedSalesProvider;
import seed.seedplusbackend.commercial.application.result.CommercialEstimatedSalesPageResult;
import seed.seedplusbackend.commercial.application.result.CommercialEstimatedSalesRowResult;
import seed.seedplusbackend.commercial.infrastructure.client.SeoulCommercialOpenApiProperties;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;

@ExtendWith(MockitoExtension.class)
@DisplayName("서울시 추정매출 Provider")
class SeoulEstimatedSalesProviderTest {

  private static final String QUARTER = "20251";

  @Mock private SeoulCommercialEstimatedSalesClientPort clientPort;
  @Mock private CommercialEstimatedSalesStorePort storePort;

  @Test
  @DisplayName("일시적인 요청 실패는 재시도한 뒤 저장한다")
  void collect_retriesRequestFailure() {
    CommercialEstimatedSalesRowResult row = mock(CommercialEstimatedSalesRowResult.class);
    given(clientPort.fetchByQuarter(QUARTER, 1, 2))
        .willThrow(new ApplicationException(ErrorCode.SEOUL_OPEN_API_REQUEST_FAILED))
        .willReturn(new CommercialEstimatedSalesPageResult(1, List.of(row)));

    provider().collect(command(), (total, fetched, cursor) -> {});

    verify(clientPort, times(2)).fetchByQuarter(QUARTER, 1, 2);
    verify(storePort).upsertAll(List.of(row));
  }

  @Test
  @DisplayName("응답 형식 오류는 재시도하지 않는다")
  void collect_doesNotRetryInvalidResponse() {
    ApplicationException exception =
        new ApplicationException(ErrorCode.SEOUL_OPEN_API_INVALID_RESPONSE);
    given(clientPort.fetchByQuarter(QUARTER, 1, 2)).willThrow(exception);

    assertThatThrownBy(() -> provider().collect(command(), (total, fetched, cursor) -> {}))
        .isSameAs(exception);

    verify(clientPort).fetchByQuarter(QUARTER, 1, 2);
  }

  private CommercialEstimatedSalesCollectCommand command() {
    return new CommercialEstimatedSalesCollectCommand(QUARTER, false);
  }

  private SeoulEstimatedSalesProvider provider() {
    return new SeoulEstimatedSalesProvider(
        clientPort,
        storePort,
        new SeoulCommercialOpenApiProperties(
            "key", "http://localhost", "VwsmTrdarSelngQq", "json", 2, 0, 0, 2),
        new ExternalApiRetryExecutor());
  }
}
