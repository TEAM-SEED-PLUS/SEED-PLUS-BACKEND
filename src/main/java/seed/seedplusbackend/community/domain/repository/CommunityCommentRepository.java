package seed.seedplusbackend.community.domain.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import seed.seedplusbackend.community.domain.entity.CommunityComment;

public interface CommunityCommentRepository {
  <S extends CommunityComment> S save(S comment);

  Optional<CommunityComment> findByIdAndPost_Id(Long id, Long postId);

  Page<CommunityComment> findByPost_Id(Long postId, Pageable pageable);
}
