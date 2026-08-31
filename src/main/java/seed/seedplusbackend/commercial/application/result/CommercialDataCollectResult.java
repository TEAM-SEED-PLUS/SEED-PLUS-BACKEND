package seed.seedplusbackend.commercial.application.result;

import seed.seedplusbackend.commercial.domain.entity.CommercialDataCollectStatus;

public record CommercialDataCollectResult(
    String dataType,
    String targetKey,
    long totalCount,
    long fetchedCount,
    boolean skipped,
    CommercialDataCollectStatus status,
    String message) {}
