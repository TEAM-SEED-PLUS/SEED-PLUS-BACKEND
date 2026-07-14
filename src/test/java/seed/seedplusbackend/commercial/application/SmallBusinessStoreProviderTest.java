package seed.seedplusbackend.commercial.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import seed.seedplusbackend.commercial.application.command.SmallBusinessStoreCollectCommand;
import seed.seedplusbackend.commercial.application.port.SmallBusinessStoreClientPort;
import seed.seedplusbackend.commercial.application.port.SmallBusinessStoreStorePort;
import seed.seedplusbackend.commercial.application.provider.SmallBusinessStoreProvider;
import seed.seedplusbackend.commercial.application.result.SmallBusinessStorePageResult;
import seed.seedplusbackend.commercial.application.result.SmallBusinessStoreRowResult;
import seed.seedplusbackend.commercial.infrastructure.client.SmallBusinessStoreOpenApiProperties;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;

@ExtendWith(MockitoExtension.class)
@DisplayName("소상공인 상가정보 Provider")
class SmallBusinessStoreProviderTest {

  @Mock private SmallBusinessStoreClientPort clientPort;
  @Mock private SmallBusinessStoreStorePort storePort;

  @Test
  @DisplayName("모든 페이지를 조회하고 저장한다")
  void collect_savesEveryPage() {
    SmallBusinessStoreCollectCommand command = command();
    SmallBusinessStoreRowResult first = mock(SmallBusinessStoreRowResult.class);
    SmallBusinessStoreRowResult second = mock(SmallBusinessStoreRowResult.class);
    SmallBusinessStoreRowResult third = mock(SmallBusinessStoreRowResult.class);
    org.mockito.Mockito.doReturn(new SmallBusinessStorePageResult(3, List.of(first, second)))
        .when(clientPort)
        .fetch(command, 1, 2);
    org.mockito.Mockito.doReturn(new SmallBusinessStorePageResult(3, List.of(third)))
        .when(clientPort)
        .fetch(command, 2, 2);
    List<Long> fetchedCounts = new ArrayList<>();

    provider().collect(command, (total, fetched, cursor) -> fetchedCounts.add(fetched));

    assertThat(fetchedCounts).containsExactly(2L, 3L);
    verify(storePort).upsertAll("9151", List.of(first, second));
    verify(storePort).upsertAll("9151", List.of(third));
  }

  @Test
  @DisplayName("외부 API 호출에 실패하면 데이터를 저장하지 않고 예외를 전달한다")
  void collect_doesNotSave_whenApiCallFails() {
    SmallBusinessStoreCollectCommand command = command();
    ApplicationException apiException =
        new ApplicationException(ErrorCode.SMALL_BUSINESS_STORE_API_REQUEST_FAILED);
    given(clientPort.fetch(command, 1, 2)).willThrow(apiException);

    assertThatThrownBy(() -> provider().collect(command, (total, fetched, cursor) -> {}))
        .isSameAs(apiException);

    verify(clientPort).fetch(command, 1, 2);
    verify(storePort, never()).upsertAll(anyString(), anyList());
  }

  @Test
  @DisplayName("일시적인 요청 실패는 재시도한 뒤 저장한다")
  void collect_retriesRequestFailure() {
    SmallBusinessStoreCollectCommand command = command();
    SmallBusinessStoreRowResult row = mock(SmallBusinessStoreRowResult.class);
    given(clientPort.fetch(command, 1, 2))
        .willThrow(new ApplicationException(ErrorCode.SMALL_BUSINESS_STORE_API_REQUEST_FAILED))
        .willReturn(new SmallBusinessStorePageResult(1, List.of(row)));

    provider(2).collect(command, (total, fetched, cursor) -> {});

    verify(clientPort, times(2)).fetch(command, 1, 2);
    verify(storePort).upsertAll("9151", List.of(row));
  }

  @Test
  @DisplayName("응답 형식 오류는 재시도하지 않는다")
  void collect_doesNotRetryInvalidResponse() {
    SmallBusinessStoreCollectCommand command = command();
    ApplicationException exception =
        new ApplicationException(ErrorCode.SMALL_BUSINESS_STORE_API_INVALID_RESPONSE);
    given(clientPort.fetch(command, 1, 2)).willThrow(exception);

    assertThatThrownBy(() -> provider(2).collect(command, (total, fetched, cursor) -> {}))
        .isSameAs(exception);

    verify(clientPort).fetch(command, 1, 2);
    verify(storePort, never()).upsertAll(anyString(), anyList());
  }

  private SmallBusinessStoreCollectCommand command() {
    return new SmallBusinessStoreCollectCommand("9151", "Q", "Q12", "Q12A01", false);
  }

  private SmallBusinessStoreProvider provider() {
    return provider(0);
  }

  private SmallBusinessStoreProvider provider(int maxRetryCount) {
    return new SmallBusinessStoreProvider(
        clientPort,
        storePort,
        new SmallBusinessStoreOpenApiProperties(
            "key", "http://localhost", "storeListInArea", "json", 2, 0, maxRetryCount));
  }
}
