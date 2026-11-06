package seed.seedplusbackend.community.infrastructure.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import seed.seedplusbackend.community.domain.entity.CommunityComment;
import seed.seedplusbackend.community.domain.repository.CommunityCommentRepository;

public interface CommunityCommentJpaRepository
    extends JpaRepository<CommunityComment, Long>, CommunityCommentRepository {
  @Override
  @EntityGraph(attributePaths = "user")
  Optional<CommunityComment> findByIdAndPost_Id(Long id, Long postId);

  @Override
  @EntityGraph(attributePaths = "user")
  Page<CommunityComment> findByPost_Id(Long postId, Pageable pageable);
}
