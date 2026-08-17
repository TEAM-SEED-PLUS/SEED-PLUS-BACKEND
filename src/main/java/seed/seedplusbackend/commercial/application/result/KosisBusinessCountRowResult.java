package seed.seedplusbackend.commercial.application.result;

import java.math.BigDecimal;

public record KosisBusinessCountRowResult(
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
    BigDecimal businessCount,
    String sourceUpdatedAt) {}
