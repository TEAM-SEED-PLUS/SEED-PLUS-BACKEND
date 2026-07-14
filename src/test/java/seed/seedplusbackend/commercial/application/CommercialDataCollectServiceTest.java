package seed.seedplusbackend.commercial.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import seed.seedplusbackend.commercial.application.command.SmallBusinessStoreCollectCommand;
import seed.seedplusbackend.commercial.application.provider.CommercialDataProvider;
import seed.seedplusbackend.commercial.application.provider.CommercialDataProviderRegistry;
import seed.seedplusbackend.commercial.application.provider.CommercialDataType;
import seed.seedplusbackend.commercial.application.result.CommercialDataCollectResult;
import seed.seedplusbackend.commercial.domain.entity.CommercialDataCollectStatus;
import seed.seedplusbackend.commercial.domain.repository.CommercialDataCollectHistoryRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("공공데이터 공통 수집 서비스")
class CommercialDataCollectServiceTest {

  private CommercialDataCollectService service;

  @Mock private CommercialDataProviderRegistry providerRegistry;
  @Mock private CommercialDataProvider provider;
  @Mock private CommercialDataCollectHistoryRepository historyRepository;

  @BeforeEach
  void setUp() {
    service = new CommercialDataCollectService(providerRegistry, historyRepository);
    org.mockito.Mockito.lenient()
        .when(historyRepository.save(org.mockito.ArgumentMatchers.any()))
        .thenAnswer(invocation -> invocation.getArgument(0));
  }

  @Test
  @DisplayName("데이터 유형에 맞는 Provider를 실행하고 진행 이력을 저장한다")
  void collect_executesSelectedProvider() {
    SmallBusinessStoreCollectCommand command = command(false);
    given(providerRegistry.get(CommercialDataType.SMALL_BUSINESS_STORE)).willReturn(provider);
    org.mockito.Mockito.doAnswer(
            invocation -> {
              seed.seedplusbackend.commercial.application.provider.CollectProgress progress =
                  invocation.getArgument(1);
              progress.update(3, 3, 2);
              return null;
            })
        .when(provider)
        .collect(org.mockito.ArgumentMatchers.eq(command), org.mockito.ArgumentMatchers.any());

    CommercialDataCollectResult result = service.collect(command);

    assertThat(result.status()).isEqualTo(CommercialDataCollectStatus.COMPLETED);
    assertThat(result.totalCount()).isEqualTo(3);
    assertThat(result.fetchedCount()).isEqualTo(3);
    verify(provider)
        .collect(org.mockito.ArgumentMatchers.eq(command), org.mockito.ArgumentMatchers.any());
  }

  @Test
  @DisplayName("이미 수집한 조건은 Provider를 실행하지 않는다")
  void collect_skipsCompletedTarget() {
    SmallBusinessStoreCollectCommand command = command(false);
    given(
            historyRepository.existsByDataTypeAndTargetKeyAndStatus(
                "SMALL_BUSINESS_STORE", command.targetKey(), CommercialDataCollectStatus.RUNNING))
        .willReturn(false);
    given(
            historyRepository.existsByDataTypeAndTargetKeyAndStatus(
                "SMALL_BUSINESS_STORE", command.targetKey(), CommercialDataCollectStatus.COMPLETED))
        .willReturn(true);

    CommercialDataCollectResult result = service.collect(command);

    assertThat(result.skipped()).isTrue();
    verify(providerRegistry, never()).get(org.mockito.ArgumentMatchers.any());
  }

  private SmallBusinessStoreCollectCommand command(boolean force) {
    return new SmallBusinessStoreCollectCommand("9151", "Q", "Q12", "Q12A01", force);
  }
}
