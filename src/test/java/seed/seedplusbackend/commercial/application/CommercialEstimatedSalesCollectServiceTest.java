package seed.seedplusbackend.commercial.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import seed.seedplusbackend.commercial.application.command.CommercialEstimatedSalesCollectCommand;
import seed.seedplusbackend.commercial.application.port.CommercialDataCollectClaimPort;
import seed.seedplusbackend.commercial.application.port.CommercialEstimatedSalesStorePort;
import seed.seedplusbackend.commercial.application.port.SeoulCommercialEstimatedSalesClientPort;
import seed.seedplusbackend.commercial.application.provider.CommercialDataProviderRegistry;
import seed.seedplusbackend.commercial.application.provider.SeoulEstimatedSalesProvider;
import seed.seedplusbackend.commercial.application.result.CommercialDataCollectResult;
import seed.seedplusbackend.commercial.application.result.CommercialEstimatedSalesPageResult;
import seed.seedplusbackend.commercial.application.result.CommercialEstimatedSalesRowResult;
import seed.seedplusbackend.commercial.domain.entity.CommercialDataCollectHistory;
import seed.seedplusbackend.commercial.domain.entity.CommercialDataCollectStatus;
import seed.seedplusbackend.commercial.domain.repository.CommercialDataCollectHistoryRepository;
import seed.seedplusbackend.commercial.infrastructure.client.SeoulCommercialOpenApiProperties;

@ExtendWith(MockitoExtension.class)
@DisplayName("상권 추정매출 수집 서비스")
class CommercialEstimatedSalesCollectServiceTest {

  private static final String DATA_TYPE = "SEOUL_COMMERCIAL_ESTIMATED_SALES";
  private static final String QUARTER = "20251";

  private CommercialDataCollectService service;

  @Mock private SeoulCommercialEstimatedSalesClientPort clientPort;
  @Mock private CommercialEstimatedSalesStorePort storePort;
  @Mock private CommercialDataCollectHistoryRepository historyRepository;
  @Mock private CommercialDataCollectClaimPort claimPort;

  @BeforeEach
  void setUp() {
    SeoulCommercialOpenApiProperties properties =
        new SeoulCommercialOpenApiProperties(
            "test-key", "http://localhost", "VwsmTrdarSelngQq", "json", 2, 0, 0, 0);
    SeoulEstimatedSalesProvider provider =
        new SeoulEstimatedSalesProvider(clientPort, storePort, properties);
    service =
        new CommercialDataCollectService(
            new CommercialDataProviderRegistry(List.of(provider)), historyRepository, claimPort);

    CommercialDataCollectHistory claimedHistory =
        CommercialDataCollectHistory.start(DATA_TYPE, QUARTER);
    org.mockito.Mockito.lenient()
        .when(
            claimPort.tryClaim(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyBoolean()))
        .thenReturn(Optional.of(1L));
    org.mockito.Mockito.lenient()
        .when(historyRepository.findById(1L))
        .thenReturn(Optional.of(claimedHistory));

    // 저장된 이력을 그대로 돌려준다.
    org.mockito.Mockito.lenient()
        .when(historyRepository.save(org.mockito.ArgumentMatchers.any()))
        .thenAnswer(invocation -> invocation.getArgument(0));
  }

  @Test
  @DisplayName("여러 페이지를 모두 조회하고 저장한다")
  void collect_savesEveryPage() {
    CommercialEstimatedSalesRowResult first = mock(CommercialEstimatedSalesRowResult.class);
    CommercialEstimatedSalesRowResult second = mock(CommercialEstimatedSalesRowResult.class);
    CommercialEstimatedSalesRowResult third = mock(CommercialEstimatedSalesRowResult.class);
    given(clientPort.fetchByQuarter(QUARTER, 1, 2))
        .willReturn(new CommercialEstimatedSalesPageResult(3, List.of(first, second)));
    given(clientPort.fetchByQuarter(QUARTER, 3, 4))
        .willReturn(new CommercialEstimatedSalesPageResult(3, List.of(third)));

    CommercialDataCollectResult result =
        service.collect(new CommercialEstimatedSalesCollectCommand(QUARTER, false));

    assertThat(result.status()).isEqualTo(CommercialDataCollectStatus.COMPLETED);
    assertThat(result.totalCount()).isEqualTo(3);
    assertThat(result.fetchedCount()).isEqualTo(3);
    assertThat(result.skipped()).isFalse();
    verify(storePort).upsertAll(List.of(first, second));
    verify(storePort).upsertAll(List.of(third));
  }

  @Test
  @DisplayName("이미 수집한 분기는 다시 수집하지 않는다")
  void collect_skipsAlreadyCollectedQuarter() {
    given(claimPort.tryClaim(DATA_TYPE, QUARTER, false)).willReturn(Optional.empty());
    given(
            historyRepository.existsByDataTypeAndTargetKeyAndStatus(
                DATA_TYPE, QUARTER, CommercialDataCollectStatus.RUNNING))
        .willReturn(false);
    CommercialDataCollectResult result =
        service.collect(new CommercialEstimatedSalesCollectCommand(QUARTER, false));

    assertThat(result.skipped()).isTrue();
    assertThat(result.status()).isEqualTo(CommercialDataCollectStatus.COMPLETED);
    verify(clientPort, never())
        .fetchByQuarter(
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.anyInt(),
            org.mockito.ArgumentMatchers.anyInt());
    verify(storePort, never()).upsertAll(org.mockito.ArgumentMatchers.anyList());
  }

  @Test
  @DisplayName("데이터가 없으면 0건으로 수집을 완료한다")
  void collect_completesWhenNoData() {
    given(clientPort.fetchByQuarter(QUARTER, 1, 2))
        .willReturn(new CommercialEstimatedSalesPageResult(0, List.of()));

    CommercialDataCollectResult result =
        service.collect(new CommercialEstimatedSalesCollectCommand(QUARTER, false));

    assertThat(result.status()).isEqualTo(CommercialDataCollectStatus.COMPLETED);
    assertThat(result.totalCount()).isZero();
    assertThat(result.fetchedCount()).isZero();
    verify(storePort, never()).upsertAll(org.mockito.ArgumentMatchers.anyList());
  }
}
