package seed.seedplusbackend.store.infrastructure.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import seed.seedplusbackend.store.domain.entity.Store;
import seed.seedplusbackend.store.domain.entity.StoreStatus;
import seed.seedplusbackend.store.domain.repository.StoreRepository;

public interface StoreJpaRepository extends JpaRepository<Store, Long>, StoreRepository {

  @Override
  <S extends Store> S save(S entity);

  @Override
  Optional<Store> findById(Long id);

  @Override
  @EntityGraph(
      attributePaths = {"building", "building.region", "building.commercialArea", "industry"})
  Optional<Store> findByIdAndStatus(Long id, StoreStatus status);

  @Override
  @EntityGraph(
      attributePaths = {"building", "building.region", "building.commercialArea", "industry"})
  @Query(
      value =
          """
          SELECT store
          FROM Store store
          JOIN store.building building
          JOIN building.region region
          JOIN store.industry industry
          WHERE store.status = :status
            AND (:buildingId IS NULL OR building.id = :buildingId)
            AND (:industryId IS NULL OR industry.id = :industryId)
            AND (:regionId IS NULL OR region.id = :regionId)
            AND (:isVacant IS NULL OR store.vacant = :isVacant)
          """,
      countQuery =
          """
          SELECT COUNT(store)
          FROM Store store
          JOIN store.building building
          JOIN building.region region
          JOIN store.industry industry
          WHERE store.status = :status
            AND (:buildingId IS NULL OR building.id = :buildingId)
            AND (:industryId IS NULL OR industry.id = :industryId)
            AND (:regionId IS NULL OR region.id = :regionId)
            AND (:isVacant IS NULL OR store.vacant = :isVacant)
          """)
  Page<Store> searchStores(
      @Param("status") StoreStatus status,
      @Param("buildingId") Long buildingId,
      @Param("industryId") Long industryId,
      @Param("regionId") Long regionId,
      @Param("isVacant") Boolean isVacant,
      Pageable pageable);

  @Override
  boolean existsByIdAndStatus(Long id, StoreStatus status);

  @Override
  void deleteById(Long id);
}
