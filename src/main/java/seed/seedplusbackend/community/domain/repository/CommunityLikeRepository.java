package seed.seedplusbackend.community.domain.repository;

import java.util.List;
import java.util.Optional;
import seed.seedplusbackend.community.domain.entity.CommunityLike;

public interface CommunityLikeRepository {
  <S extends CommunityLike> S save(S like);

  Optional<CommunityLike> findByPost_IdAndUser_Id(Long postId, Long userId);

  void delete(CommunityLike like);

  List<Long> findLikedPostIds(Long userId, List<Long> postIds);
}
