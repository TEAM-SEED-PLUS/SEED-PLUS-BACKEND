package seed.seedplusbackend.builderstore.infrastructure.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreComment;
import seed.seedplusbackend.builderstore.domain.repository.BuilderStoreCommentRepository;

public interface BuilderStoreCommentJpaRepository
    extends JpaRepository<BuilderStoreComment, Long>, BuilderStoreCommentRepository {

  @Override
  <S extends BuilderStoreComment> S save(S entity);

  @Override
  Optional<BuilderStoreComment> findById(Long id);

  @Override
  void deleteById(Long id);
}
