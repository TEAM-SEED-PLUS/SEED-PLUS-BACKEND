package seed.seedplusbackend.region.infrastructure.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import seed.seedplusbackend.region.domain.entity.Region;
import seed.seedplusbackend.region.domain.repository.RegionRepository;

public interface RegionJpaRepository extends JpaRepository<Region, Long>, RegionRepository {

  @Override
  <S extends Region> S save(S entity);

  @Override
  Optional<Region> findById(Long id);

  @Override
  void deleteById(Long id);
}
