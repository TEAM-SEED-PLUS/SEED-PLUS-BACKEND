package seed.seedplusbackend.community.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import seed.seedplusbackend.community.application.query.CommunityCommentPageQuery;

@Schema(description = "댓글 작성 시각·ID 오름차순 페이지 요청")
public record CommunityCommentPageRequest(
    @Schema(description = "0부터 시작하는 페이지", defaultValue = "0") @Min(0) Integer page,
    @Schema(description = "페이지 크기 (1~100)", defaultValue = "20") @Min(1) @Max(100) Integer size) {
  public CommunityCommentPageQuery toQuery() {
    return new CommunityCommentPageQuery(CommunityPageableFactory.comments(page, size));
  }
}
