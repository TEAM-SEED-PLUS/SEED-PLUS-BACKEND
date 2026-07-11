package seed.seedplusbackend.commercial.presentation.dto;

import seed.seedplusbackend.commercial.application.result.CommercialEstimatedSalesCollectResult;

public record CommercialEstimatedSalesCollectResponse(
    String dataType,
    String targetKey,
    long totalCount,
    long fetchedCount,
    boolean skipped,
    String status,
    String message) {

  public static CommercialEstimatedSalesCollectResponse from(
      CommercialEstimatedSalesCollectResult result) {
    return new CommercialEstimatedSalesCollectResponse(
        result.dataType(),
        result.targetKey(),
        result.totalCount(),
        result.fetchedCount(),
        result.skipped(),
        result.status().name(),
        result.message());
  }
}
