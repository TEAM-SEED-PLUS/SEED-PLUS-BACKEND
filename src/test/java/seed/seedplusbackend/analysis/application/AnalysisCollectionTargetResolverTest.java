package seed.seedplusbackend.analysis.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import seed.seedplusbackend.analysis.application.command.AnalysisCollectionTarget;
import seed.seedplusbackend.analysis.application.command.SmallBusinessCollectionTarget;
import seed.seedplusbackend.commercial.application.LatestEstimatedSalesQuarterResolver;
import seed.seedplusbackend.commercial.application.command.SmallBusinessStoreQueryType;
import seed.seedplusbackend.industry.application.IndustryHierarchyResolver;
import seed.seedplusbackend.industry.application.result.IndustryHierarchyResult;
import seed.seedplusbackend.region.application.RegionResolver;
import seed.seedplusbackend.region.domain.entity.Region;

@ExtendWith(MockitoExtension.class)
@DisplayName("분석 데이터 수집 대상 조회기")
class AnalysisCollectionTargetResolverTest {

  @Mock private RegionResolver regionResolver;
  @Mock private IndustryHierarchyResolver industryHierarchyResolver;
  @Mock private LatestEstimatedSalesQuarterResolver latestEstimatedSalesQuarterResolver;

  @Test
  @DisplayName("법정동이 속한 시군구 단위로 소상공인 점포 수집 대상을 만든다")
  void resolvesSigunguCollectionTarget() {
    given(regionResolver.resolveLegalDong("1168010100"))
        .willReturn(org.mockito.Mockito.mock(Region.class));
    given(industryHierarchyResolver.resolve("G22199"))
        .willReturn(new IndustryHierarchyResult("G2", "G221", "G22199"));
    given(latestEstimatedSalesQuarterResolver.resolve()).willReturn("20262");

    AnalysisCollectionTarget target = resolver().resolve("1168010100", "G22199");

    assertThat(target.estimatedSalesQuarter()).isEqualTo("20262");
    assertThat(target.smallBusinessTargets())
        .containsExactly(
            new SmallBusinessCollectionTarget(
                "11680", "G2", "G221", "G22199", SmallBusinessStoreQueryType.SIGUNGU));
  }

  private AnalysisCollectionTargetResolver resolver() {
    return new AnalysisCollectionTargetResolver(
        regionResolver, industryHierarchyResolver, latestEstimatedSalesQuarterResolver);
  }
}
