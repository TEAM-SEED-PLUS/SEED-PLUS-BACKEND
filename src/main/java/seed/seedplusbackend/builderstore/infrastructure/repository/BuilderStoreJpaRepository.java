package seed.seedplusbackend.builderstore.infrastructure.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStore;
import seed.seedplusbackend.builderstore.domain.repository.BuilderStoreRepository;

public interface BuilderStoreJpaRepository
    extends JpaRepository<BuilderStore, Long>, BuilderStoreRepository {

  @Override
  <S extends BuilderStore> S save(S entity);

  @Override
  Optional<BuilderStore> findById(Long id);

  @Override
  void deleteById(Long id);
}
