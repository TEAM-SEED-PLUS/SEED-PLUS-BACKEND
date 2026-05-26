package seed.seedplusbackend.builderstore.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import seed.seedplusbackend.builderstore.application.query.BuilderStorePageQuery;

@Schema(description = "내 빌더스토어 목록 요청")
public record BuilderStorePageRequest(
    @Schema(description = "페이지 번호 (0부터 시작)", example = "0") @Min(0) Integer page,
    @Schema(description = "페이지 크기", example = "20") @Min(1) @Max(100) Integer size,
    @Schema(description = "정렬 조건", example = "uploadedAt,desc") String sort) {

  public BuilderStorePageQuery toQuery() {
    return new BuilderStorePageQuery(BuilderStorePageableFactory.create(page, size, sort));
  }
}
