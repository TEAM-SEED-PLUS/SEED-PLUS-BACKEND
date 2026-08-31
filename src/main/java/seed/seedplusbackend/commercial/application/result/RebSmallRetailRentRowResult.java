package seed.seedplusbackend.commercial.application.result;

import java.math.BigDecimal;

public record RebSmallRetailRentRowResult(
    String sourceAreaKey,
    int sourceRowNumber,
    String areaName,
    String areaPath,
    int areaLevel,
    int referenceYear,
    int referenceQuarter,
    BigDecimal rentPerSquareMeterThousandKrw) {}
