package seed.seedplusbackend.builderstore.infrastructure.repository;

import java.util.List;
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
  List<BuilderStoreImage> findByBuilderStore_IdOrderByDisplayOrderAscIdAsc(Long builderStoreId);

  @Override
  void deleteByBuilderStore_Id(Long builderStoreId);

  @Override
  void flush();

  @Override
  void deleteById(Long id);
}
