package seed.seedplusbackend.community.infrastructure.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import seed.seedplusbackend.community.domain.entity.CommunityLike;
import seed.seedplusbackend.community.domain.repository.CommunityLikeRepository;

public interface CommunityLikeJpaRepository
    extends JpaRepository<CommunityLike, Long>, CommunityLikeRepository {
  @Override
  @Query(
      "select l.post.id from CommunityLike l where l.user.id = :userId and l.post.id in :postIds")
  List<Long> findLikedPostIds(@Param("userId") Long userId, @Param("postIds") List<Long> postIds);
}
