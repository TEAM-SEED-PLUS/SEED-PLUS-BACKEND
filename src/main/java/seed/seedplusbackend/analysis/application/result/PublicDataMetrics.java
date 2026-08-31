package seed.seedplusbackend.analysis.application.result;

import java.math.BigDecimal;
import java.util.List;

public record PublicDataMetrics(
    Long monthlySalesAmount,
    Integer storeCountInCommercialArea,
    BigDecimal districtAverageSalesAmount,
    BigDecimal cityAverageSalesAmount,
    Integer storeZoneOne,
    Integer storeListInArea,
    Integer storeListInRadius,
    Integer competitorCount,
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
