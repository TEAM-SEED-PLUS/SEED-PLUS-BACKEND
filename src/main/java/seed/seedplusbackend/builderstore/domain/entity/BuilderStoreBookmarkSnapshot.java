package seed.seedplusbackend.builderstore.domain.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record BuilderStoreBookmarkSnapshot(
    String estimatedSalesQuarter,
    Long estimatedSalesAmount,
    Integer businessSurvivalYear,
    BigDecimal survivalRate,
    Integer businessCountYear,
    BigDecimal activeBusinessCount,
    BigDecimal newBusinessCount,
    BigDecimal closedBusinessCount,
    OffsetDateTime storeInfoCollectedAt,
    Integer storeCount,
    Integer rentReferenceYear,
    Integer rentReferenceQuarter,
    BigDecimal rentPerSquareMeterThousandKrw) {}
