package seed.seedplusbackend.analysis.application.command;

import java.math.BigDecimal;
import java.util.List;

public record ProfitAnalysisLambdaCommand(
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
    BigDecimal districtAverageSalesAmount,
    BigDecimal cityAverageSalesAmount,
    Integer storeZoneOne,
    Integer storeListInArea,
    Integer storeListInRadius,
    Integer competitorCount,
    boolean fallbackUsed,
    List<String> dataSources) {}
