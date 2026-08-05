package seed.seedplusbackend.commercial.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import seed.seedplusbackend.commercial.application.command.KosisBusinessSurvivalCollectCommand;
import seed.seedplusbackend.commercial.application.port.CommercialDataCollectClaimPort;
import seed.seedplusbackend.commercial.application.port.KosisBusinessSurvivalClientPort;
import seed.seedplusbackend.commercial.application.port.KosisBusinessSurvivalStorePort;
import seed.seedplusbackend.commercial.application.provider.CommercialDataProviderRegistry;
import seed.seedplusbackend.commercial.application.provider.ExternalApiRetryExecutor;
import seed.seedplusbackend.commercial.application.provider.KosisBusinessSurvivalProvider;
import seed.seedplusbackend.commercial.application.result.CommercialDataCollectResult;
import seed.seedplusbackend.commercial.application.result.KosisBusinessSurvivalRowResult;
import seed.seedplusbackend.commercial.domain.entity.CommercialDataCollectHistory;
import seed.seedplusbackend.commercial.domain.entity.CommercialDataCollectStatus;
import seed.seedplusbackend.commercial.domain.repository.CommercialDataCollectHistoryRepository;
import seed.seedplusbackend.commercial.infrastructure.client.KosisBusinessSurvivalOpenApiProperties;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;

@ExtendWith(MockitoExtension.class)
class KosisBusinessSurvivalCollectServiceTest {

  private static final String DATA_TYPE = "KOSIS_BUSINESS_SURVIVAL_RATE";
  private static final String TARGET_KEY = "PERIOD:2021-2022";

  private CommercialDataCollectService service;

  @Mock private KosisBusinessSurvivalClientPort clientPort;
  @Mock private KosisBusinessSurvivalStorePort storePort;
  @Mock private CommercialDataCollectHistoryRepository historyRepository;
  @Mock private CommercialDataCollectClaimPort claimPort;

  @BeforeEach
  void setUp() {
    KosisBusinessSurvivalOpenApiProperties properties =
        new KosisBusinessSurvivalOpenApiProperties(
            "key", "https://kosis.kr", "/openapi", "101", "DT_2BD1003", 0, 0, 0, 0);
    KosisBusinessSurvivalProvider provider =
        new KosisBusinessSurvivalProvider(
            clientPort, storePort, properties, new ExternalApiRetryExecutor());
    service =
        new CommercialDataCollectService(
            new CommercialDataProviderRegistry(List.of(provider)), historyRepository, claimPort);
  }

  @Test
  void collect_upsertsAllRowsAndCompletesHistory() {
    KosisBusinessSurvivalCollectCommand command =
        new KosisBusinessSurvivalCollectCommand(2021, 2022, null, false);
    KosisBusinessSurvivalRowResult row =
        new KosisBusinessSurvivalRowResult(
            "101",
            "DT_2BD1003",
            "산업별 신생기업 생존율",
            "A",
            "전체 산업",
            "산업분류",
            "T01",
            "1년 생존율",
            "%",
            "Y",
            2022,
            new BigDecimal("64.1"),
            "2025-12-01");
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
    KosisBusinessSurvivalCollectCommand command =
        new KosisBusinessSurvivalCollectCommand(2021, 2022, null, false);
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
    KosisBusinessSurvivalCollectCommand command =
        new KosisBusinessSurvivalCollectCommand(2021, 2022, null, false);
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
  void command_rejectsMixedPeriodAndLatestConditions() {
    assertThatThrownBy(() -> new KosisBusinessSurvivalCollectCommand(2021, 2022, 3, false))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
