package seed.seedplusbackend.building.domain.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import seed.seedplusbackend.building.domain.entity.Building;

public interface BuildingRepository {

  <S extends Building> S save(S entity);

  Optional<Building> findById(Long id);

  Optional<Building> findWithRegionAndCommercialAreaById(Long id);

  Optional<Building> findFirstByRegion_IdAndCommercialArea_IdAndAddressOrderByIdAsc(
      Long regionId, Long commercialAreaId, String address);

  Optional<Building> findNearestWithinDistance(
      Long regionId,
      Long commercialAreaId,
      BigDecimal latitude,
      BigDecimal longitude,
      double distanceMeters);

  List<Building> findAll();

  Page<Building> findAll(Pageable pageable);

  Page<Building> findByRegion_Id(Long regionId, Pageable pageable);

  Page<Building> findByCommercialArea_Id(Long commercialAreaId, Pageable pageable);

  Page<Building> findByRegion_IdAndCommercialArea_Id(
      Long regionId, Long commercialAreaId, Pageable pageable);

  boolean existsById(Long id);

  void delete(Building entity);

  void deleteById(Long id);

  long count();
}
