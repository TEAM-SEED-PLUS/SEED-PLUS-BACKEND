package seed.seedplusbackend.metrics.infrastructure.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import seed.seedplusbackend.metrics.domain.entity.StoreOperationMonthlyMetric;
import seed.seedplusbackend.metrics.domain.repository.StoreOperationMonthlyMetricRepository;

public interface StoreOperationMonthlyMetricJpaRepository
    extends JpaRepository<StoreOperationMonthlyMetric, Long>,
        StoreOperationMonthlyMetricRepository {

  @Override
  <S extends StoreOperationMonthlyMetric> S save(S entity);

  @Override
  Optional<StoreOperationMonthlyMetric> findById(Long id);

  @Override
  @Query(
      """
      SELECT metric
      FROM StoreOperationMonthlyMetric metric
      WHERE metric.store.id = :storeId
        AND (:startMonth IS NULL OR metric.referenceMonth >= :startMonth)
        AND (:endMonth IS NULL OR metric.referenceMonth <= :endMonth)
      ORDER BY metric.referenceMonth ASC
      """)
  List<StoreOperationMonthlyMetric> findByStoreIdAndReferenceMonthRange(
      @Param("storeId") Long storeId,
      @Param("startMonth") LocalDate startMonth,
      @Param("endMonth") LocalDate endMonth);

  @Override
  void deleteById(Long id);
}
