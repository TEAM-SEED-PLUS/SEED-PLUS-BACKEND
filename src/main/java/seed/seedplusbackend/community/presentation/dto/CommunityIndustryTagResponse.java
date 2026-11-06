package seed.seedplusbackend.community.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import seed.seedplusbackend.community.application.result.CommunityIndustryTagResult;

public record CommunityIndustryTagResponse(
    Long industryId, String name, @Schema(description = "LARGE / MEDIUM / SMALL") String level) {
  public static CommunityIndustryTagResponse from(CommunityIndustryTagResult r) {
    return new CommunityIndustryTagResponse(r.industryId(), r.name(), r.level().name());
  }
}
