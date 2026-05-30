package seed.seedplusbackend.building.infrastructure.repository;

import java.math.BigDecimal;
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
  Optional<Building> findFirstByRegion_IdAndCommercialArea_IdAndAddressOrderByIdAsc(
      Long regionId, Long commercialAreaId, String address);

  @Override
  @Query(
      value =
          """
          select *
          from buildings b
          where b.region_id = :regionId
            and b.commercial_area_id = :commercialAreaId
            and b.location is not null
            and ST_DWithin(
              b.location,
              cast(ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326) as geography),
              :distanceMeters
            )
          order by ST_Distance(
              b.location,
              cast(ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326) as geography)
            ) asc,
            b.building_id asc
          limit 1
          """,
      nativeQuery = true)
  Optional<Building> findNearestWithinDistance(
      @Param("regionId") Long regionId,
      @Param("commercialAreaId") Long commercialAreaId,
      @Param("latitude") BigDecimal latitude,
      @Param("longitude") BigDecimal longitude,
      @Param("distanceMeters") double distanceMeters);

  @Override
  void deleteById(Long id);
}
