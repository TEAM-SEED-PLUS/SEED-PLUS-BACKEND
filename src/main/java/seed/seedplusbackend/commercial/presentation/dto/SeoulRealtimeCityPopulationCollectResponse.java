package seed.seedplusbackend.commercial.presentation.dto;

import seed.seedplusbackend.commercial.application.result.CommercialDataCollectResult;

public record SeoulRealtimeCityPopulationCollectResponse(
    String dataType,
    String targetKey,
    long totalCount,
    long fetchedCount,
    boolean skipped,
    String status,
    String message) {
  public static SeoulRealtimeCityPopulationCollectResponse from(
      CommercialDataCollectResult result) {
    return new SeoulRealtimeCityPopulationCollectResponse(
        result.dataType(),
        result.targetKey(),
        result.totalCount(),
        result.fetchedCount(),
        result.skipped(),
        result.status().name(),
        result.message());
  }
}
