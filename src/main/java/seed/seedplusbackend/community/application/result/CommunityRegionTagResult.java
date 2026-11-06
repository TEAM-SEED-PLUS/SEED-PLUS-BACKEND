package seed.seedplusbackend.community.application.result;

import seed.seedplusbackend.region.domain.entity.Region;

public record CommunityRegionTagResult(Long regionId, String sido, String sigungu) {
  public static CommunityRegionTagResult from(Region region) {
    return new CommunityRegionTagResult(region.getId(), region.getSido(), region.getSigungu());
  }
}
