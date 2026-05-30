package seed.seedplusbackend.store.infrastructure.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import seed.seedplusbackend.store.domain.entity.StoreImage;
import seed.seedplusbackend.store.domain.repository.StoreImageRepository;

public interface StoreImageJpaRepository
    extends JpaRepository<StoreImage, Long>, StoreImageRepository {

  @Override
  <S extends StoreImage> S save(S entity);

  @Override
  Optional<StoreImage> findById(Long id);

  @Override
  void deleteById(Long id);
}
