package seed.seedplusbackend.community.application.result;

import seed.seedplusbackend.industry.domain.entity.Industry;
import seed.seedplusbackend.industry.domain.entity.IndustryLevel;

public record CommunityIndustryTagResult(Long industryId, String name, IndustryLevel level) {
  public static CommunityIndustryTagResult from(Industry industry) {
    return new CommunityIndustryTagResult(
        industry.getId(), industry.getName(), industry.getLevel());
  }
}
