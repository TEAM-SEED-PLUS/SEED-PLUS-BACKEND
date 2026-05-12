package seed.seedplusbackend.builderstore.infrastructure.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreImage;
import seed.seedplusbackend.builderstore.domain.repository.BuilderStoreImageRepository;

public interface BuilderStoreImageJpaRepository
    extends JpaRepository<BuilderStoreImage, Long>, BuilderStoreImageRepository {

  @Override
  <S extends BuilderStoreImage> S save(S entity);

  @Override
  Optional<BuilderStoreImage> findById(Long id);

  @Override
  void deleteById(Long id);
}
