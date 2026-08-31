package seed.seedplusbackend.analysis.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import seed.seedplusbackend.analysis.application.command.AnalysisCollectionTarget;
import seed.seedplusbackend.analysis.application.command.SmallBusinessCollectionTarget;
import seed.seedplusbackend.commercial.application.LatestEstimatedSalesQuarterResolver;
import seed.seedplusbackend.commercial.application.RegionExternalCodeResolver;
import seed.seedplusbackend.commercial.domain.entity.ExternalDataSource;
import seed.seedplusbackend.industry.application.IndustryHierarchyResolver;
import seed.seedplusbackend.industry.application.result.IndustryHierarchyResult;

@Component
@RequiredArgsConstructor
public class AnalysisCollectionTargetResolver {

  private final RegionExternalCodeResolver regionExternalCodeResolver;
  private final IndustryHierarchyResolver industryHierarchyResolver;
  private final LatestEstimatedSalesQuarterResolver latestEstimatedSalesQuarterResolver;

  public AnalysisCollectionTarget resolve(String regionCode, String industryCode) {
    List<String> commercialAreaCodes =
        regionExternalCodeResolver.resolve(regionCode, ExternalDataSource.SMALL_BUSINESS_STORE);
    IndustryHierarchyResult industryHierarchy = industryHierarchyResolver.resolve(industryCode);
    String estimatedSalesQuarter = latestEstimatedSalesQuarterResolver.resolve();

    List<SmallBusinessCollectionTarget> smallBusinessTargets =
        commercialAreaCodes.stream()
            .map(code -> toSmallBusinessTarget(code, industryHierarchy))
            .toList();

    return new AnalysisCollectionTarget(estimatedSalesQuarter, smallBusinessTargets);
  }

  private SmallBusinessCollectionTarget toSmallBusinessTarget(
      String commercialAreaCode, IndustryHierarchyResult industryHierarchy) {
    return new SmallBusinessCollectionTarget(
        commercialAreaCode,
        industryHierarchy.largeIndustryCode(),
        industryHierarchy.mediumIndustryCode(),
        industryHierarchy.smallIndustryCode());
  }
}
