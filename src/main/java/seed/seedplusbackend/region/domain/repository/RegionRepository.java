package seed.seedplusbackend.region.domain.repository;

import java.util.List;
import java.util.Optional;
import seed.seedplusbackend.region.domain.entity.Region;

public interface RegionRepository {

  <S extends Region> S save(S entity);

  Optional<Region> findById(Long id);

  List<Region> findAll();

  boolean existsById(Long id);

  void delete(Region entity);

  void deleteById(Long id);

  long count();
}
