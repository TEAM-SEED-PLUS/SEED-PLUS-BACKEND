package seed.seedplusbackend.analysis.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import seed.seedplusbackend.analysis.application.command.AnalysisCollectionTarget;
import seed.seedplusbackend.analysis.application.command.SmallBusinessCollectionTarget;
import seed.seedplusbackend.commercial.application.LatestEstimatedSalesQuarterResolver;
import seed.seedplusbackend.commercial.application.command.SmallBusinessStoreQueryType;
import seed.seedplusbackend.industry.application.IndustryHierarchyResolver;
import seed.seedplusbackend.industry.application.result.IndustryHierarchyResult;
import seed.seedplusbackend.region.application.RegionResolver;

@Component
@RequiredArgsConstructor
public class AnalysisCollectionTargetResolver {

  private static final int SIGUNGU_CODE_LENGTH = 5;

  private final RegionResolver regionResolver;
  private final IndustryHierarchyResolver industryHierarchyResolver;
  private final LatestEstimatedSalesQuarterResolver latestEstimatedSalesQuarterResolver;

  public AnalysisCollectionTarget resolve(String regionCode, String industryCode) {
    regionResolver.resolveLegalDong(regionCode);
    IndustryHierarchyResult industryHierarchy = industryHierarchyResolver.resolve(industryCode);
    String estimatedSalesQuarter = latestEstimatedSalesQuarterResolver.resolve();

    SmallBusinessCollectionTarget smallBusinessTarget =
        toSmallBusinessTarget(regionCode.substring(0, SIGUNGU_CODE_LENGTH), industryHierarchy);

    return new AnalysisCollectionTarget(estimatedSalesQuarter, List.of(smallBusinessTarget));
  }

  private SmallBusinessCollectionTarget toSmallBusinessTarget(
      String commercialAreaCode, IndustryHierarchyResult industryHierarchy) {
    return new SmallBusinessCollectionTarget(
        commercialAreaCode,
        industryHierarchy.largeIndustryCode(),
        industryHierarchy.mediumIndustryCode(),
        industryHierarchy.smallIndustryCode(),
        SmallBusinessStoreQueryType.SIGUNGU);
  }
}
