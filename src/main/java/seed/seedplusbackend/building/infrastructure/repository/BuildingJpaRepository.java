package seed.seedplusbackend.building.infrastructure.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import seed.seedplusbackend.building.domain.entity.Building;
import seed.seedplusbackend.building.domain.repository.BuildingRepository;

public interface BuildingJpaRepository extends JpaRepository<Building, Long>, BuildingRepository {

  @Override
  <S extends Building> S save(S entity);

  @Override
  Optional<Building> findById(Long id);

  @Override
  @EntityGraph(attributePaths = {"region", "commercialArea"})
  @Query("select b from Building b where b.id = :id")
  Optional<Building> findWithRegionAndCommercialAreaById(@Param("id") Long id);

  @Override
  void deleteById(Long id);
}
