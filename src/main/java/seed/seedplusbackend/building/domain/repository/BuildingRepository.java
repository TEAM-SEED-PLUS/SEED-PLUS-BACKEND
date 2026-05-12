package seed.seedplusbackend.building.domain.repository;

import java.util.List;
import java.util.Optional;
import seed.seedplusbackend.building.domain.entity.Building;

public interface BuildingRepository {

  <S extends Building> S save(S entity);

  Optional<Building> findById(Long id);

  List<Building> findAll();

  boolean existsById(Long id);

  void delete(Building entity);

  void deleteById(Long id);

  long count();
}
