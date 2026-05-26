package seed.seedplusbackend.builderstore.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStore;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreVisibilityStatus;
import seed.seedplusbackend.commercial.presentation.dto.CommercialAreaResponse;
import seed.seedplusbackend.industry.presentation.dto.IndustryResponse;
import seed.seedplusbackend.region.presentation.dto.RegionResponse;

@Schema(description = "빌더스토어 요약 응답")
public record BuilderStoreSummaryResponse(
    @Schema(description = "빌더스토어 ID", example = "1") Long builderStoreId,
    @Schema(description = "빌더스토어명", example = "강남 샐러드 창업안") String name,
    @Schema(description = "면적", example = "40") int area,
    @Schema(description = "예상 월 매출", example = "50000000") long expectedMonthlySales,
    @Schema(description = "예상 수익률", example = "12.50") BigDecimal expectedProfitRate,
    @Schema(description = "투자 회수 기간(개월)", example = "36") int investmentPaybackMonths,
    @Schema(description = "입지 점수", example = "80") int propertyScore,
    @Schema(description = "월 임대료", example = "2000000") long monthlyRent,
    @Schema(description = "보증금", example = "20000000") long deposit,
    @Schema(description = "투자 금액", example = "100000000") long investmentAmount,
    @Schema(description = "설명", example = "테스트 빌더스토어") String description,
    @Schema(description = "좋아요 수", example = "10") long likeCount,
    @Schema(description = "댓글 수", example = "3") long commentCount,
    @Schema(description = "공개 상태", example = "PUBLIC")
        BuilderStoreVisibilityStatus visibilityStatus,
    @Schema(description = "업로드 일시") OffsetDateTime uploadedAt,
    @Schema(description = "지역") RegionResponse region,
    @Schema(description = "상권") CommercialAreaResponse commercialArea,
    @Schema(description = "업종") IndustryResponse industry) {

  public static BuilderStoreSummaryResponse from(BuilderStore builderStore) {
    return new BuilderStoreSummaryResponse(
        builderStore.getId(),
        builderStore.getName(),
        builderStore.getArea(),
        builderStore.getExpectedMonthlySales(),
        builderStore.getExpectedProfitRate(),
        builderStore.getInvestmentPaybackMonths(),
        builderStore.getPropertyScore(),
        builderStore.getMonthlyRent(),
        builderStore.getDeposit(),
        builderStore.getInvestmentAmount(),
        builderStore.getDescription(),
        builderStore.getLikeCount(),
        builderStore.getCommentCount(),
        builderStore.getVisibilityStatus(),
        builderStore.getUploadedAt(),
        RegionResponse.from(builderStore.getRegion()),
        CommercialAreaResponse.from(builderStore.getCommercialArea()),
        IndustryResponse.from(builderStore.getIndustry()));
  }
}
