package seed.seedplusbackend.commercial.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import seed.seedplusbackend.commercial.application.result.PagedCommercialAreaResult;
import seed.seedplusbackend.global.response.PageInfo;

@Schema(description = "상권 목록 페이지 응답")
public record PagedCommercialAreaResponse(
    @Schema(description = "상권 목록") List<CommercialAreaResponse> content,
    @Schema(description = "페이지 정보") PageInfo pageInfo) {

  public PagedCommercialAreaResponse {
    content = content == null ? List.of() : List.copyOf(content);
  }

  public static PagedCommercialAreaResponse from(PagedCommercialAreaResult result) {
    return new PagedCommercialAreaResponse(
        CommercialAreaResponse.from(result.content()),
        PageInfo.of(result.page(), result.size(), result.totalElements()));
  }
}
