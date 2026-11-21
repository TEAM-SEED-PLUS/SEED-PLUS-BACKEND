package seed.seedplusbackend.news.presentation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import seed.seedplusbackend.global.response.PageInfo;
import seed.seedplusbackend.global.response.PageResponse;
import seed.seedplusbackend.news.application.result.NewsDigestResult;

@Schema(description = "뉴스 다이제스트와 브리핑 기준일")
public record NewsDigestResponse(
    List<NewsArticleResponse> content,
    PageInfo pageInfo,
    @JsonInclude(JsonInclude.Include.ALWAYS)
        @Schema(
            description = "브리핑 기준일. 날짜 지정 시 요청일, 생략 시 실제 제공일. 제공 가능한 자료가 없으면 null",
            nullable = true,
            example = "2026-11-20")
        LocalDate digestDate) {
  public static NewsDigestResponse from(NewsDigestResult result) {
    var page = PageResponse.from(result.articles(), NewsArticleResponse::from);
    return new NewsDigestResponse(page.content(), page.pageInfo(), result.digestDate());
  }
}
