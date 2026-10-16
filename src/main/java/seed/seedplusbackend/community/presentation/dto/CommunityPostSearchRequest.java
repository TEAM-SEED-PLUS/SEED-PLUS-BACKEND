package seed.seedplusbackend.community.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;
import seed.seedplusbackend.community.application.query.CommunityPostSearchQuery;

@Schema(description = "지역 내 OR, 업종 내 OR, 지역·업종·키워드 간 AND. 상위 업종은 하위 태그 포함")
public record CommunityPostSearchRequest(
    @Schema(description = "제목·본문·태그명 검색", example = "강남구") @Size(max = 200) String keyword,
    @Schema(description = "지역 ID 목록 (SIGUNGU)") @Size(max = 2)
        List<@NotNull @Positive Long> regionIds,
    @Schema(description = "활성 업종 ID 목록") @Size(max = 2) List<@NotNull @Positive Long> industryIds,
    @Schema(description = "0부터 시작하는 페이지", defaultValue = "0") @Min(0) Integer page,
    @Schema(description = "페이지 크기 (1~100)", defaultValue = "20") @Min(1) @Max(100) Integer size,
    @Schema(
            description = "createdAt,desc 또는 likeCount,desc. 동률은 ID 역순",
            defaultValue = "createdAt,desc")
        String sort) {
  public CommunityPostSearchQuery toQuery() {
    return new CommunityPostSearchQuery(
        keyword, regionIds, industryIds, CommunityPageableFactory.posts(page, size, sort));
  }
}
