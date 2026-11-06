package seed.seedplusbackend.community.application;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import seed.seedplusbackend.community.domain.CommunityContentPolicy;
import seed.seedplusbackend.global.error.DomainException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.industry.domain.entity.Industry;
import seed.seedplusbackend.industry.domain.entity.IndustryStatus;
import seed.seedplusbackend.industry.domain.repository.IndustryRepository;
import seed.seedplusbackend.region.domain.entity.Region;
import seed.seedplusbackend.region.domain.entity.RegionCodeType;
import seed.seedplusbackend.region.domain.repository.RegionRepository;

@Component
@RequiredArgsConstructor
public class CommunityTagResolver {
  private final RegionRepository regionRepository;
  private final IndustryRepository industryRepository;

  public Set<Region> regions(List<Long> ids) {
    Set<Region> regions = new LinkedHashSet<>();
    for (Long id : CommunityContentPolicy.tagIds(ids)) {
      Region region =
          regionRepository
              .findById(id)
              .orElseThrow(() -> new DomainException(ErrorCode.INVALID_COMMUNITY_TAG));
      if (region.getCodeType() != RegionCodeType.SIGUNGU) {
        throw new DomainException(ErrorCode.INVALID_COMMUNITY_TAG);
      }
      regions.add(region);
    }
    return regions;
  }

  public Set<Industry> industries(List<Long> ids) {
    Set<Industry> industries = new LinkedHashSet<>();
    for (Long id : CommunityContentPolicy.tagIds(ids)) {
      industries.add(
          industryRepository
              .findByIdAndStatus(id, IndustryStatus.ACTIVE)
              .orElseThrow(() -> new DomainException(ErrorCode.INVALID_COMMUNITY_TAG)));
    }
    return industries;
  }
}
