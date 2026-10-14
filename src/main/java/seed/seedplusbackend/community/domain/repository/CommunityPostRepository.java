package seed.seedplusbackend.community.domain.repository;

import java.util.List;
import java.util.Optional;
import seed.seedplusbackend.community.domain.entity.CommunityPost;

public interface CommunityPostRepository {
  <S extends CommunityPost> S save(S post);

  Optional<CommunityPost> findById(Long id);

  Optional<CommunityPost> findLiveForUpdate(Long id);

  List<CommunityPost> loadRegions(List<Long> ids);

  List<CommunityPost> loadIndustries(List<Long> ids);
}
