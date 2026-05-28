package seed.seedplusbackend.analysis.application;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import seed.seedplusbackend.analysis.application.command.ProfitAnalysisCommand;
import seed.seedplusbackend.analysis.application.command.ProfitAnalysisLambdaCommand;
import seed.seedplusbackend.analysis.application.command.SurvivalAnalysisCommand;
import seed.seedplusbackend.analysis.application.command.SurvivalAnalysisLambdaCommand;
import seed.seedplusbackend.analysis.application.port.AnalysisLambdaClient;
import seed.seedplusbackend.analysis.application.result.ProfitAnalysisResult;
import seed.seedplusbackend.analysis.application.result.SurvivalAnalysisResult;
import seed.seedplusbackend.global.cache.CacheSpec;
import seed.seedplusbackend.global.cache.CacheStore;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.industry.domain.entity.Industry;
import seed.seedplusbackend.industry.domain.entity.IndustryStatus;
import seed.seedplusbackend.industry.domain.repository.IndustryRepository;
import seed.seedplusbackend.region.domain.entity.Region;
import seed.seedplusbackend.region.domain.repository.RegionRepository;

@Service
@RequiredArgsConstructor
public class AnalysisService {

  private final AnalysisLambdaClient analysisLambdaClient;
  private final CacheStore cacheStore;
  private final RegionRepository regionRepository;
  private final IndustryRepository industryRepository;

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
    Region region = getRegion(command.regionCode());
    Industry industry = getIndustry(command.industryCode());
    return new ProfitAnalysisLambdaCommand(
        industry.getName(),
        regionName(region),
        command.area(),
        command.invest(),
        command.rent(),
        command.premium(),
        command.staff());
  }

  private SurvivalAnalysisLambdaCommand toLambda(SurvivalAnalysisCommand command) {
    Region region = getRegion(command.regionCode());
    Industry industry = getIndustry(command.industryCode());
    return new SurvivalAnalysisLambdaCommand(
        regionName(region),
        industry.getName(),
        command.area(),
        command.rent(),
        command.deposit(),
        command.avgSales(),
        command.salesGrowth(),
        command.density(),
        command.vacancy(),
        command.traffic(),
        command.churn(),
        command.startupType(),
        command.avgSalesAmt());
  }

  private Region getRegion(String regionCode) {
    return regionRepository
        .findByCode(regionCode)
        .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND_REGION));
  }

  private Industry getIndustry(String industryCode) {
    return industryRepository
        .findByIndustryCodeAndStatus(industryCode, IndustryStatus.ACTIVE)
        .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND_INDUSTRY));
  }

  private String regionName(Region region) {
    return Stream.of(region.getSido(), region.getSigungu(), region.getDong())
        .filter(value -> value != null && !value.isBlank())
        .reduce((left, right) -> left + " " + right)
        .orElse("");
  }

  private String profitCacheKey(ProfitAnalysisCommand command) {
    return String.join(
        "|",
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
        "regionCode=" + encode(command.regionCode()),
        "industryCode=" + encode(command.industryCode()),
        "area=" + number(command.area()),
        "rent=" + number(command.rent()),
        "deposit=" + number(command.deposit()),
        "avgSales=" + number(command.avgSales()),
        "salesGrowth=" + number(command.salesGrowth()),
        "density=" + number(command.density()),
        "vacancy=" + number(command.vacancy()),
        "traffic=" + number(command.traffic()),
        "churn=" + number(command.churn()),
        "startupType=" + encode(command.startupType()),
        "avgSalesAmt=" + number(command.avgSalesAmt()));
  }

  private String number(BigDecimal value) {
    return value.stripTrailingZeros().toPlainString();
  }

  private String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }
}
