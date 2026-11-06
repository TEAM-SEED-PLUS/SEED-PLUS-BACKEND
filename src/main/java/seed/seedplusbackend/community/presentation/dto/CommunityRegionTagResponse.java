package seed.seedplusbackend.community.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import seed.seedplusbackend.community.application.result.CommunityRegionTagResult;

@Schema(description = "자치구 태그")
public record CommunityRegionTagResponse(Long regionId, String sido, String sigungu) {
  public static CommunityRegionTagResponse from(CommunityRegionTagResult r) {
    return new CommunityRegionTagResponse(r.regionId(), r.sido(), r.sigungu());
  }
}
