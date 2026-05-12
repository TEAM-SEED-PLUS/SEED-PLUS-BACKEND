package seed.seedplusbackend.building.infrastructure.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import seed.seedplusbackend.building.domain.entity.Building;
import seed.seedplusbackend.building.domain.repository.BuildingRepository;

public interface BuildingJpaRepository extends JpaRepository<Building, Long>, BuildingRepository {

  @Override
  <S extends Building> S save(S entity);

  @Override
  Optional<Building> findById(Long id);

  @Override
  void deleteById(Long id);
}
