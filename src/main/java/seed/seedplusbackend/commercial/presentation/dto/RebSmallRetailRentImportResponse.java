package seed.seedplusbackend.commercial.presentation.dto;

import seed.seedplusbackend.commercial.application.result.CommercialDataCollectResult;

public record RebSmallRetailRentImportResponse(
    String dataType,
    String targetKey,
    long totalCount,
    long importedCount,
    boolean skipped,
    String status,
    String message) {

  public static RebSmallRetailRentImportResponse from(CommercialDataCollectResult result) {
    return new RebSmallRetailRentImportResponse(
        result.dataType(),
        result.targetKey(),
        result.totalCount(),
        result.fetchedCount(),
        result.skipped(),
        result.status().name(),
        result.message());
  }
}
