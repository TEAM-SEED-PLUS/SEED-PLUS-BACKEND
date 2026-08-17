package seed.seedplusbackend.analysis.application;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import seed.seedplusbackend.analysis.application.command.ProfitAnalysisCommand;
import seed.seedplusbackend.analysis.application.command.ProfitAnalysisLambdaCommand;
import seed.seedplusbackend.analysis.application.command.SurvivalAnalysisCommand;
import seed.seedplusbackend.analysis.application.command.SurvivalAnalysisLambdaCommand;
import seed.seedplusbackend.analysis.application.port.AnalysisLambdaClient;
import seed.seedplusbackend.analysis.application.port.PublicDataResolver;
import seed.seedplusbackend.analysis.application.result.ProfitAnalysisResult;
import seed.seedplusbackend.analysis.application.result.PublicDataMetrics;
import seed.seedplusbackend.analysis.application.result.SurvivalAnalysisResult;
import seed.seedplusbackend.global.cache.CacheSpec;
import seed.seedplusbackend.global.cache.CacheStore;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.industry.domain.entity.IndustryStatus;
import seed.seedplusbackend.industry.domain.repository.IndustryRepository;
import seed.seedplusbackend.region.application.RegionResolver;

@Service
@RequiredArgsConstructor
public class AnalysisService {

  private static final String MVP_REGION_CODE = "1168010100";
  private static final String MVP_REGION_NAME = "서울특별시 강남구 역삼동";
  private static final String MVP_INDUSTRY_CODE = "I101";
  private static final String MVP_INDUSTRY_NAME = "카페";

  private final AnalysisLambdaClient analysisLambdaClient;
  private final CacheStore cacheStore;
  private final RegionResolver regionResolver;
  private final IndustryRepository industryRepository;
  private final PublicDataResolver publicDataResolver;

  public ProfitAnalysisResult calculateProfit(Long userId, ProfitAnalysisCommand command) {
    validateAuthenticated(userId);
    String cacheKey = profitCacheKey(command);

    return cacheStore
        .get(CacheSpec.ANALYSIS_PROFIT_RESULT, cacheKey, ProfitAnalysisResult.class)
        .orElseGet(
            () -> {
              ProfitAnalysisResult result = analysisLambdaClient.requestProfit(toLambda(command));
              cacheStore.put(CacheSpec.ANALYSIS_PROFIT_RESULT, cacheKey, result);
              return result;
            });
  }

  public SurvivalAnalysisResult calculateSurvival(Long userId, SurvivalAnalysisCommand command) {
    validateAuthenticated(userId);
    String cacheKey = survivalCacheKey(command);

    return cacheStore
        .get(CacheSpec.ANALYSIS_SURVIVAL_RESULT, cacheKey, SurvivalAnalysisResult.class)
        .orElseGet(
            () -> {
              SurvivalAnalysisResult result =
                  analysisLambdaClient.requestSurvival(toLambda(command));
              cacheStore.put(CacheSpec.ANALYSIS_SURVIVAL_RESULT, cacheKey, result);
              return result;
            });
  }

  private void validateAuthenticated(Long userId) {
    if (userId == null) {
      throw new ApplicationException(ErrorCode.UNAUTHORIZED);
    }
  }

  private ProfitAnalysisLambdaCommand toLambda(ProfitAnalysisCommand command) {
    String regionName = resolveRegionName(command.regionCode());
    String industryName = resolveIndustryName(command.industryCode());
    PublicDataMetrics metrics = publicDataResolver.resolve(regionName, industryName);
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
    PublicDataMetrics metrics = publicDataResolver.resolve(regionName, industryName);
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
        .orElseGet(
            () -> {
              if (MVP_INDUSTRY_CODE.equals(industryCode)) {
                return MVP_INDUSTRY_NAME;
              }
              throw new ApplicationException(ErrorCode.NOT_FOUND_INDUSTRY);
            });
  }

  private String resolveRegionName(String regionCode) {
    try {
      return regionResolver.resolveLegalDongName(regionCode);
    } catch (ApplicationException exception) {
      if (exception.getErrorCode() == ErrorCode.NOT_FOUND_REGION
          && MVP_REGION_CODE.equals(regionCode)) {
        return MVP_REGION_NAME;
      }
      throw exception;
    }
  }

  private String profitCacheKey(ProfitAnalysisCommand command) {
    return String.join(
        "|",
        "storeName=" + encode(command.storeName()),
        "industryCode=" + encode(command.industryCode()),
        "regionCode=" + encode(command.regionCode()),
        "area=" + number(command.area()),
        "invest=" + number(command.invest()),
        "rent=" + number(command.rent()),
        "premium=" + number(command.premium()),
        "staff=" + command.staff());
  }

  private String survivalCacheKey(SurvivalAnalysisCommand command) {
    return String.join(
        "|",
        "storeName=" + encode(command.storeName()),
        "regionCode=" + encode(command.regionCode()),
        "industryCode=" + encode(command.industryCode()),
        "area=" + number(command.area()),
        "invest=" + number(command.invest()),
        "rent=" + number(command.rent()),
        "premium=" + number(command.premium()),
        "staff=" + command.staff());
  }

  private String number(BigDecimal value) {
    return value.stripTrailingZeros().toPlainString();
  }

  private String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }
}
