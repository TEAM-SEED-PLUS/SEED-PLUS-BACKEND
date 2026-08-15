package seed.seedplusbackend.commercial.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import seed.seedplusbackend.commercial.application.command.KosisBusinessCountCollectCommand;
import seed.seedplusbackend.commercial.application.exception.KosisBusinessCountApiRequestException;
import seed.seedplusbackend.commercial.application.port.CommercialDataCollectClaimPort;
import seed.seedplusbackend.commercial.application.port.KosisBusinessCountClientPort;
import seed.seedplusbackend.commercial.application.port.KosisBusinessCountStorePort;
import seed.seedplusbackend.commercial.application.provider.CommercialDataProviderRegistry;
import seed.seedplusbackend.commercial.application.provider.ExternalApiRetryExecutor;
import seed.seedplusbackend.commercial.application.provider.KosisBusinessCountProvider;
import seed.seedplusbackend.commercial.application.result.CommercialDataCollectResult;
import seed.seedplusbackend.commercial.application.result.KosisBusinessCountRowResult;
import seed.seedplusbackend.commercial.domain.entity.CommercialDataCollectHistory;
import seed.seedplusbackend.commercial.domain.entity.CommercialDataCollectStatus;
import seed.seedplusbackend.commercial.domain.repository.CommercialDataCollectHistoryRepository;
import seed.seedplusbackend.commercial.infrastructure.client.KosisBusinessCountOpenApiProperties;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;

@ExtendWith(MockitoExtension.class)
class KosisBusinessCountCollectServiceTest {

  private static final String DATA_TYPE = "KOSIS_BUSINESS_COUNT";
  private static final String TARGET_KEY = "PERIOD:2021-2023";

  private CommercialDataCollectService service;

  @Mock private KosisBusinessCountClientPort clientPort;
  @Mock private KosisBusinessCountStorePort storePort;
  @Mock private CommercialDataCollectHistoryRepository historyRepository;
  @Mock private CommercialDataCollectClaimPort claimPort;

  @BeforeEach
  void setUp() {
    KosisBusinessCountOpenApiProperties properties =
        new KosisBusinessCountOpenApiProperties(
            "key", "https://kosis.kr", "/openapi", "101", "DT_1BD1001", 0, 0, 0, 2);
    KosisBusinessCountProvider provider =
        new KosisBusinessCountProvider(
            clientPort, storePort, properties, new ExternalApiRetryExecutor());
    service =
        new CommercialDataCollectService(
            new CommercialDataProviderRegistry(List.of(provider)), historyRepository, claimPort);
  }

  @Test
  void collect_upsertsAllRowsAndCompletesHistory() {
    KosisBusinessCountCollectCommand command =
        new KosisBusinessCountCollectCommand(2021, 2023, null, false);
    KosisBusinessCountRowResult row = row();
    CommercialDataCollectHistory history =
        CommercialDataCollectHistory.start(DATA_TYPE, TARGET_KEY);
    given(claimPort.tryClaim(DATA_TYPE, TARGET_KEY, false)).willReturn(Optional.of(1L));
    given(historyRepository.findById(1L)).willReturn(Optional.of(history));
    given(clientPort.fetch(command)).willReturn(List.of(row));

    CommercialDataCollectResult result = service.collect(command);

    assertThat(result.status()).isEqualTo(CommercialDataCollectStatus.COMPLETED);
    assertThat(result.fetchedCount()).isEqualTo(1);
    verify(storePort).upsertAll(List.of(row));
  }

  @Test
  void collect_doesNotCallKosisWhenTargetWasAlreadyClaimed() {
    KosisBusinessCountCollectCommand command =
        new KosisBusinessCountCollectCommand(2021, 2023, null, false);
    given(claimPort.tryClaim(DATA_TYPE, TARGET_KEY, false)).willReturn(Optional.empty());
    given(
            historyRepository.existsByDataTypeAndTargetKeyAndStatus(
                DATA_TYPE, TARGET_KEY, CommercialDataCollectStatus.RUNNING))
        .willReturn(true);

    CommercialDataCollectResult result = service.collect(command);

    assertThat(result.skipped()).isTrue();
    assertThat(result.status()).isEqualTo(CommercialDataCollectStatus.RUNNING);
    verify(clientPort, never()).fetch(command);
  }

  @Test
  void collect_failsWhenKosisReturnsNoRows() {
    KosisBusinessCountCollectCommand command =
        new KosisBusinessCountCollectCommand(2021, 2023, null, false);
    CommercialDataCollectHistory history =
        CommercialDataCollectHistory.start(DATA_TYPE, TARGET_KEY);
    given(claimPort.tryClaim(DATA_TYPE, TARGET_KEY, false)).willReturn(Optional.of(1L));
    given(historyRepository.findById(1L)).willReturn(Optional.of(history));
    given(clientPort.fetch(command)).willReturn(List.of());

    assertThatThrownBy(() -> service.collect(command))
        .isInstanceOfSatisfying(
            ApplicationException.class,
            exception ->
                assertThat(exception.getErrorCode())
                    .isEqualTo(ErrorCode.KOSIS_OPEN_API_INVALID_RESPONSE));

    assertThat(history.getStatus()).isEqualTo(CommercialDataCollectStatus.FAILED);
    verify(storePort, never()).upsertAll(List.of());
    verify(historyRepository).save(history);
  }

  @Test
  void collect_doesNotRetryClientError() {
    KosisBusinessCountCollectCommand command =
        new KosisBusinessCountCollectCommand(2021, 2023, null, false);
    CommercialDataCollectHistory history =
        CommercialDataCollectHistory.start(DATA_TYPE, TARGET_KEY);
    given(claimPort.tryClaim(DATA_TYPE, TARGET_KEY, false)).willReturn(Optional.of(1L));
    given(historyRepository.findById(1L)).willReturn(Optional.of(history));
    given(clientPort.fetch(command)).willThrow(new KosisBusinessCountApiRequestException(false));

    assertThatThrownBy(() -> service.collect(command))
        .isInstanceOf(KosisBusinessCountApiRequestException.class);

    verify(clientPort, times(1)).fetch(command);
    assertThat(history.getStatus()).isEqualTo(CommercialDataCollectStatus.FAILED);
  }

  private KosisBusinessCountRowResult row() {
    return new KosisBusinessCountRowResult(
        "101",
        "DT_1BD1001",
        "산업별 기업수",
        "A",
        "전체 산업",
        "산업별",
        "T01",
        "활동기업",
        "개",
        "Y",
        2023,
        new BigDecimal("1234567"),
        "2025-12-01");
  }
}
