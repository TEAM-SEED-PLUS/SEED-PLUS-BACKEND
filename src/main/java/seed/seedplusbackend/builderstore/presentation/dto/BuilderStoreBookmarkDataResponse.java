package seed.seedplusbackend.builderstore.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreBookmark;

@Schema(description = "저장 카드에 반영된 공공데이터 스냅샷")
public record BuilderStoreBookmarkDataResponse(
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
    BigDecimal rentPerSquareMeterThousandKrw,
    OffsetDateTime refreshedAt) {

  public static BuilderStoreBookmarkDataResponse from(BuilderStoreBookmark bookmark) {
    return new BuilderStoreBookmarkDataResponse(
        bookmark.getEstimatedSalesQuarter(),
        bookmark.getEstimatedSalesAmount(),
        bookmark.getBusinessSurvivalYear(),
        bookmark.getSurvivalRate(),
        bookmark.getBusinessCountYear(),
        bookmark.getActiveBusinessCount(),
        bookmark.getNewBusinessCount(),
        bookmark.getClosedBusinessCount(),
        bookmark.getStoreInfoCollectedAt(),
        bookmark.getStoreCount(),
        bookmark.getRentReferenceYear(),
        bookmark.getRentReferenceQuarter(),
        bookmark.getRentPerSquareMeterThousandKrw(),
        bookmark.getDataRefreshedAt());
  }
}
