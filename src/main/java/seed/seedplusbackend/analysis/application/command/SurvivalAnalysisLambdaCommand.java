package seed.seedplusbackend.analysis.application.command;

import java.math.BigDecimal;
import java.util.List;

public record SurvivalAnalysisLambdaCommand(
    String storeName,
    String industry,
    String region,
    BigDecimal area,
    BigDecimal invest,
    BigDecimal rent,
    BigDecimal premium,
    Integer staff,
    Long monthlySalesAmount,
    Integer storeCountInCommercialArea,
    BigDecimal salesGrowthRate,
    Integer storeDensity,
    BigDecimal vacancyRate,
    Integer trafficIndex,
    BigDecimal survivalRate,
    BigDecimal closedBusinesses,
    BigDecimal activeBusinesses,
    BigDecimal newBusinesses,
    boolean fallbackUsed,
    List<String> dataSources) {}
