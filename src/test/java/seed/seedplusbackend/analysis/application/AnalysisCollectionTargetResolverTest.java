package seed.seedplusbackend.analysis.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import seed.seedplusbackend.analysis.application.command.AnalysisCollectionTarget;
import seed.seedplusbackend.analysis.application.command.SmallBusinessCollectionTarget;
import seed.seedplusbackend.commercial.application.CommercialAreaExternalCodeResolver;
import seed.seedplusbackend.commercial.domain.entity.ExternalDataSource;
import seed.seedplusbackend.industry.application.IndustryHierarchyResolver;
import seed.seedplusbackend.industry.application.result.IndustryHierarchyResult;

@ExtendWith(MockitoExtension.class)
@DisplayName("분석 데이터 수집 대상 조회기")
class AnalysisCollectionTargetResolverTest {

  @Mock private CommercialAreaExternalCodeResolver commercialAreaExternalCodeResolver;
  @Mock private IndustryHierarchyResolver industryHierarchyResolver;

  @Test
  @DisplayName("지역의 모든 외부 상권에 동일한 업종 계층을 적용한다")
  void resolvesCollectionTargetsForAllCommercialAreas() {
    given(
            commercialAreaExternalCodeResolver.resolve(
                "1168010100", ExternalDataSource.SMALL_BUSINESS_STORE))
        .willReturn(List.of("10117", "10118"));
    given(industryHierarchyResolver.resolve("G22199"))
        .willReturn(new IndustryHierarchyResult("G2", "G221", "G22199"));

    AnalysisCollectionTarget target = resolver().resolve("1168010100", "G22199", "20262");

    assertThat(target.estimatedSalesQuarter()).isEqualTo("20262");
    assertThat(target.smallBusinessTargets())
        .containsExactly(
            new SmallBusinessCollectionTarget("10117", "G2", "G221", "G22199"),
            new SmallBusinessCollectionTarget("10118", "G2", "G221", "G22199"));
  }

  private AnalysisCollectionTargetResolver resolver() {
    return new AnalysisCollectionTargetResolver(
        commercialAreaExternalCodeResolver, industryHierarchyResolver);
  }
}
