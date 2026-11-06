package seed.seedplusbackend.community.infrastructure.repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import seed.seedplusbackend.community.domain.entity.CommunityPost;
import seed.seedplusbackend.community.domain.repository.CommunityPostRepository;

public interface CommunityPostJpaRepository
    extends JpaRepository<CommunityPost, Long>, CommunityPostRepository {
  @Override
  @EntityGraph(attributePaths = "user")
  Optional<CommunityPost> findById(Long id);

  @Override
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select p from CommunityPost p where p.id = :id and p.deletedAt is null")
  Optional<CommunityPost> findLiveForUpdate(@Param("id") Long id);

  @Override
  @Query("select distinct p from CommunityPost p left join fetch p.regions where p.id in :ids")
  List<CommunityPost> loadRegions(@Param("ids") List<Long> ids);

  @Override
  @Query("select distinct p from CommunityPost p left join fetch p.industries where p.id in :ids")
  List<CommunityPost> loadIndustries(@Param("ids") List<Long> ids);
}
