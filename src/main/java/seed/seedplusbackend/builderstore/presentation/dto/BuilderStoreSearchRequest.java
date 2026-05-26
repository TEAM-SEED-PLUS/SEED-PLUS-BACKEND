package seed.seedplusbackend.builderstore.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import seed.seedplusbackend.builderstore.application.query.BuilderStoreSearchQuery;

@Schema(description = "빌더스토어 검색 요청")
public record BuilderStoreSearchRequest(
    @Schema(description = "페이지 번호 (0부터 시작)", example = "0") @Min(0) Integer page,
    @Schema(description = "페이지 크기", example = "20") @Min(1) @Max(100) Integer size,
    @Schema(description = "정렬 조건", example = "uploadedAt,desc") String sort,
    @Schema(description = "지역 ID", example = "1") @Positive Long regionId,
    @Schema(description = "상권 ID", example = "1") @Positive Long commercialAreaId,
    @Schema(description = "업종 ID", example = "1") @Positive Long industryId,
    @Schema(description = "최소 면적", example = "10") @Min(0) Integer minArea,
    @Schema(description = "최대 면적", example = "100") @Min(0) Integer maxArea) {

  public BuilderStoreSearchQuery toQuery() {
    return new BuilderStoreSearchQuery(
        regionId,
        commercialAreaId,
        industryId,
        minArea,
        maxArea,
        BuilderStorePageableFactory.create(page, size, sort));
  }

  @AssertTrue(message = "최소 면적은 최대 면적보다 클 수 없습니다.")
  public boolean isValidAreaRange() {
    return minArea == null || maxArea == null || minArea <= maxArea;
  }
}
