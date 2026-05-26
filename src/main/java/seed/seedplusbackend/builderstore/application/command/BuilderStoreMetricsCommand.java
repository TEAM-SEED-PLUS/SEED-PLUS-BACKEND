package seed.seedplusbackend.builderstore.application.command;

import java.math.BigDecimal;

public record BuilderStoreMetricsCommand(
    Integer area,
    Long expectedMonthlySales,
    BigDecimal expectedProfitRate,
    Integer investmentPaybackMonths,
    Long monthlyRent,
    Long deposit,
    Long investmentAmount) {}
