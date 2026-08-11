package seed.seedplusbackend.commercial.application.result;

import java.math.BigDecimal;

public record KosisBusinessSurvivalRowResult(
    String organizationId,
    String tableId,
    String tableName,
    String industryCode,
    String industryName,
    String classificationName,
    String itemId,
    String itemName,
    String unitName,
    String periodType,
    int referenceYear,
    BigDecimal survivalRate,
    String sourceUpdatedAt) {}
