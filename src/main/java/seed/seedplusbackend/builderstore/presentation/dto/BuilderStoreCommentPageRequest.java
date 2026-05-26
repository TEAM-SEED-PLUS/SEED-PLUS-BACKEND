package seed.seedplusbackend.builderstore.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import seed.seedplusbackend.builderstore.application.query.BuilderStoreCommentPageQuery;

@Schema(description = "빌더스토어 댓글 목록 요청")
public record BuilderStoreCommentPageRequest(
    @Schema(description = "페이지 번호 (0부터 시작)", example = "0") @Min(0) Integer page,
    @Schema(description = "페이지 크기", example = "20") @Min(1) @Max(100) Integer size) {

  public BuilderStoreCommentPageQuery toQuery() {
    int resolvedPage = page == null ? 0 : page;
    int resolvedSize = size == null ? 20 : size;
    return new BuilderStoreCommentPageQuery(
        PageRequest.of(
            resolvedPage,
            resolvedSize,
            Sort.by(Sort.Order.asc("createdAt"), Sort.Order.asc("id"))));
  }
}
