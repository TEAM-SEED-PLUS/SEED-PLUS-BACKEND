package seed.seedplusbackend.commercial.presentation.dto;

import seed.seedplusbackend.commercial.application.result.CommercialDataCollectResult;

public record SmallBusinessStoreCollectResponse(
    String dataType,
    String targetKey,
    long totalCount,
    long fetchedCount,
    boolean skipped,
    String status,
    String message) {

  public static SmallBusinessStoreCollectResponse from(CommercialDataCollectResult result) {
    return new SmallBusinessStoreCollectResponse(
        result.dataType(),
        result.targetKey(),
        result.totalCount(),
        result.fetchedCount(),
        result.skipped(),
        result.status().name(),
        result.message());
  }
}
