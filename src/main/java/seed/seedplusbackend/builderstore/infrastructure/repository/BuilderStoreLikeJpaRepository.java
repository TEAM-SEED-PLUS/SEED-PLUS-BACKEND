package seed.seedplusbackend.builderstore.infrastructure.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreLike;
import seed.seedplusbackend.builderstore.domain.repository.BuilderStoreLikeRepository;

public interface BuilderStoreLikeJpaRepository
    extends JpaRepository<BuilderStoreLike, Long>, BuilderStoreLikeRepository {

  @Override
  <S extends BuilderStoreLike> S save(S entity);

  @Override
  Optional<BuilderStoreLike> findById(Long id);

  @Override
  Optional<BuilderStoreLike> findByBuilderStore_IdAndUser_Id(Long builderStoreId, Long userId);

  @Override
  boolean existsByBuilderStore_IdAndUser_Id(Long builderStoreId, Long userId);

  @Override
  void deleteById(Long id);
}
