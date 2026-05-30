package seed.seedplusbackend.builderstore.infrastructure.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStore;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreVisibilityStatus;
import seed.seedplusbackend.builderstore.domain.repository.BuilderStoreRepository;

public interface BuilderStoreJpaRepository
    extends JpaRepository<BuilderStore, Long>, BuilderStoreRepository {

  @Override
  <S extends BuilderStore> S save(S entity);

  @Override
  Optional<BuilderStore> findById(Long id);

  @Override
  @EntityGraph(attributePaths = {"user", "region", "commercialArea", "industry", "baseBuilding"})
  Optional<BuilderStore> findByIdAndVisibilityStatus(
      Long id, BuilderStoreVisibilityStatus visibilityStatus);

  @Override
  @EntityGraph(attributePaths = {"user", "region", "commercialArea", "industry", "baseBuilding"})
  Optional<BuilderStore> findByIdAndVisibilityStatusNot(
      Long id, BuilderStoreVisibilityStatus visibilityStatus);

  @Override
  @EntityGraph(attributePaths = {"user", "region", "commercialArea", "industry", "baseBuilding"})
  @Query(
      value =
          """
          SELECT builderStore
          FROM BuilderStore builderStore
          JOIN builderStore.region region
          JOIN builderStore.commercialArea commercialArea
          JOIN builderStore.industry industry
          WHERE builderStore.visibilityStatus = seed.seedplusbackend.builderstore.domain.entity.BuilderStoreVisibilityStatus.PUBLIC
            AND (:regionId IS NULL OR region.id = :regionId)
            AND (:commercialAreaId IS NULL OR commercialArea.id = :commercialAreaId)
            AND (:industryId IS NULL OR industry.id = :industryId)
            AND (:minArea IS NULL OR builderStore.area >= :minArea)
            AND (:maxArea IS NULL OR builderStore.area <= :maxArea)
          """,
      countQuery =
          """
          SELECT COUNT(builderStore)
          FROM BuilderStore builderStore
          JOIN builderStore.region region
          JOIN builderStore.commercialArea commercialArea
          JOIN builderStore.industry industry
          WHERE builderStore.visibilityStatus = seed.seedplusbackend.builderstore.domain.entity.BuilderStoreVisibilityStatus.PUBLIC
            AND (:regionId IS NULL OR region.id = :regionId)
            AND (:commercialAreaId IS NULL OR commercialArea.id = :commercialAreaId)
            AND (:industryId IS NULL OR industry.id = :industryId)
            AND (:minArea IS NULL OR builderStore.area >= :minArea)
            AND (:maxArea IS NULL OR builderStore.area <= :maxArea)
          """)
  Page<BuilderStore> searchPublic(
      @Param("regionId") Long regionId,
      @Param("commercialAreaId") Long commercialAreaId,
      @Param("industryId") Long industryId,
      @Param("minArea") Integer minArea,
      @Param("maxArea") Integer maxArea,
      Pageable pageable);

  @Override
  @EntityGraph(attributePaths = {"user", "region", "commercialArea", "industry", "baseBuilding"})
  Page<BuilderStore> findByUser_IdAndVisibilityStatusNot(
      Long userId, BuilderStoreVisibilityStatus visibilityStatus, Pageable pageable);

  @Override
  void deleteById(Long id);
}
