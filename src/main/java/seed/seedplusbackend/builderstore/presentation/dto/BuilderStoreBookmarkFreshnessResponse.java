package seed.seedplusbackend.builderstore.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import seed.seedplusbackend.builderstore.application.result.BuilderStoreBookmarkResult;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreBookmark;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreBookmarkSnapshot;

@Schema(description = "저장 카드 공공데이터 최신화 상태")
public record BuilderStoreBookmarkFreshnessResponse(
    String latestEstimatedSalesLabel,
    boolean estimatedSalesUpdateAvailable,
    String estimatedSalesUpdateMessage,
    boolean otherDataUpdateAvailable,
    String otherDataUpdateMessage,
    String savedEstimatedSalesQuarter,
    String currentEstimatedSalesQuarter,
    Integer savedBusinessSurvivalYear,
    Integer currentBusinessSurvivalYear,
    Integer savedBusinessCountYear,
    Integer currentBusinessCountYear,
    OffsetDateTime savedStoreInfoCollectedAt,
    OffsetDateTime currentStoreInfoCollectedAt,
    String savedRentPeriod,
    String currentRentPeriod) {

  public static BuilderStoreBookmarkFreshnessResponse from(BuilderStoreBookmarkResult result) {
    BuilderStoreBookmark saved = result.bookmark();
    BuilderStoreBookmarkSnapshot current = result.currentSnapshot();
    return new BuilderStoreBookmarkFreshnessResponse(
        quarterLabel(current.estimatedSalesQuarter()),
        result.estimatedSalesUpdateAvailable(),
        result.estimatedSalesUpdateAvailable() ? "최신 데이터가 있습니다" : null,
        result.otherDataUpdateAvailable(),
        result.otherDataUpdateAvailable() ? "새로운 변동사항이 있습니다" : null,
        saved.getEstimatedSalesQuarter(),
        current.estimatedSalesQuarter(),
        saved.getBusinessSurvivalYear(),
        current.businessSurvivalYear(),
        saved.getBusinessCountYear(),
        current.businessCountYear(),
        saved.getStoreInfoCollectedAt(),
        current.storeInfoCollectedAt(),
        period(saved.getRentReferenceYear(), saved.getRentReferenceQuarter()),
        period(current.rentReferenceYear(), current.rentReferenceQuarter()));
  }

  private static String quarterLabel(String quarter) {
    if (quarter == null || quarter.length() != 5) {
      return null;
    }
    return "%s년 %s분기 기준".formatted(quarter.substring(0, 4), quarter.substring(4));
  }

  private static String period(Integer year, Integer quarter) {
    return year == null || quarter == null ? null : "%dQ%d".formatted(year, quarter);
  }
}
