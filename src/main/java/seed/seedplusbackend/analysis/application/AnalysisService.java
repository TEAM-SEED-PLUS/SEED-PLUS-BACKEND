package seed.seedplusbackend.analysis.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import seed.seedplusbackend.analysis.application.command.ProfitAnalysisCommand;
import seed.seedplusbackend.analysis.application.command.ProfitAnalysisLambdaCommand;
import seed.seedplusbackend.analysis.application.command.SurvivalAnalysisCommand;
import seed.seedplusbackend.analysis.application.command.SurvivalAnalysisLambdaCommand;
import seed.seedplusbackend.analysis.application.port.AnalysisLambdaClient;
import seed.seedplusbackend.analysis.application.port.PublicDataResolver;
import seed.seedplusbackend.analysis.application.result.AnalysisDataCollectionResult;
import seed.seedplusbackend.analysis.application.result.ProfitAnalysisResult;
import seed.seedplusbackend.analysis.application.result.PublicDataMetrics;
import seed.seedplusbackend.analysis.application.result.SurvivalAnalysisResult;
import seed.seedplusbackend.analysis.domain.entity.AnalysisCollectionRunStatus;
import seed.seedplusbackend.analysis.domain.entity.AnalysisCollectionType;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.industry.domain.entity.IndustryStatus;
import seed.seedplusbackend.industry.domain.repository.IndustryRepository;
import seed.seedplusbackend.region.application.RegionResolver;

@Service
@RequiredArgsConstructor
public class AnalysisService {

  private final AnalysisLambdaClient analysisLambdaClient;
  private final RegionResolver regionResolver;
  private final IndustryRepository industryRepository;
  private final PublicDataResolver publicDataResolver;
  private final AnalysisDataCollectionCoordinator collectionCoordinator;

  public ProfitAnalysisResult calculateProfit(Long userId, ProfitAnalysisCommand command) {
    validateAuthenticated(userId);
    collectPublicData(
        userId,
        AnalysisCollectionType.PROFIT,
        command.regionCode(),
        command.industryCode(),
        command.collectionRunId());
    return analysisLambdaClient.requestProfit(toLambda(command));
  }

  public SurvivalAnalysisResult calculateSurvival(Long userId, SurvivalAnalysisCommand command) {
    validateAuthenticated(userId);
    collectPublicData(
        userId,
        AnalysisCollectionType.SURVIVAL,
        command.regionCode(),
        command.industryCode(),
        command.collectionRunId());
    return analysisLambdaClient.requestSurvival(toLambda(command));
  }

  private void collectPublicData(
      Long userId,
      AnalysisCollectionType type,
      String regionCode,
      String industryCode,
      Long collectionRunId) {
    AnalysisDataCollectionResult result =
        collectionRunId == null
            ? collectionCoordinator.collect(userId, type, regionCode, industryCode)
            : collectionCoordinator.retry(userId, collectionRunId, type, regionCode, industryCode);
    if (result.status() != AnalysisCollectionRunStatus.COMPLETED) {
      throw new ApplicationException(
          ErrorCode.ANALYSIS_DATA_COLLECTION_FAILED,
          "runId=%s, failedDataTypes=%s"
              .formatted(result.runId(), String.join(",", result.failedDataTypes())));
    }
  }

  private void validateAuthenticated(Long userId) {
    if (userId == null) {
      throw new ApplicationException(ErrorCode.UNAUTHORIZED);
    }
  }

  private ProfitAnalysisLambdaCommand toLambda(ProfitAnalysisCommand command) {
    String regionName = resolveRegionName(command.regionCode());
    String industryName = resolveIndustryName(command.industryCode());
    PublicDataMetrics metrics =
        publicDataResolver.resolve(command.regionCode(), command.industryCode());
    return new ProfitAnalysisLambdaCommand(
        command.storeName(),
        industryName,
        regionName,
        command.area(),
        command.invest(),
        command.rent(),
        command.premium(),
        command.staff(),
        metrics.monthlySalesAmount(),
        metrics.storeCountInCommercialArea(),
        metrics.districtAverageSalesAmount(),
        metrics.cityAverageSalesAmount(),
        metrics.storeZoneOne(),
        metrics.storeListInArea(),
        metrics.storeListInRadius(),
        metrics.competitorCount(),
        metrics.fallbackUsed(),
        metrics.dataSources());
  }

  private SurvivalAnalysisLambdaCommand toLambda(SurvivalAnalysisCommand command) {
    String regionName = resolveRegionName(command.regionCode());
    String industryName = resolveIndustryName(command.industryCode());
    PublicDataMetrics metrics =
        publicDataResolver.resolve(command.regionCode(), command.industryCode());
    return new SurvivalAnalysisLambdaCommand(
        command.storeName(),
        industryName,
        regionName,
        command.area(),
        command.invest(),
        command.rent(),
        command.premium(),
        command.staff(),
        metrics.monthlySalesAmount(),
        metrics.storeCountInCommercialArea(),
        metrics.salesGrowthRate(),
        metrics.storeDensity(),
        metrics.vacancyRate(),
        metrics.trafficIndex(),
        metrics.survivalRate(),
        metrics.closedBusinesses(),
        metrics.activeBusinesses(),
        metrics.newBusinesses(),
        metrics.fallbackUsed(),
        metrics.dataSources());
  }

  private String resolveIndustryName(String industryCode) {
    return industryRepository
        .findByIndustryCodeAndStatus(industryCode, IndustryStatus.ACTIVE)
        .map(industry -> industry.getName())
        .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND_INDUSTRY));
  }

  private String resolveRegionName(String regionCode) {
    return regionResolver.resolveLegalDongName(regionCode);
  }
}
