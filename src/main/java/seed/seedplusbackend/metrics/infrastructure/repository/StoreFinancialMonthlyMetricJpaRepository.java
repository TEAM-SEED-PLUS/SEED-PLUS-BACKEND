package seed.seedplusbackend.metrics.infrastructure.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import seed.seedplusbackend.metrics.domain.entity.StoreFinancialMonthlyMetric;
import seed.seedplusbackend.metrics.domain.repository.StoreFinancialMonthlyMetricRepository;

public interface StoreFinancialMonthlyMetricJpaRepository
    extends JpaRepository<StoreFinancialMonthlyMetric, Long>,
        StoreFinancialMonthlyMetricRepository {

  @Override
  <S extends StoreFinancialMonthlyMetric> S save(S entity);

  @Override
  Optional<StoreFinancialMonthlyMetric> findById(Long id);

  @Override
  @Query(
      """
      SELECT metric
      FROM StoreFinancialMonthlyMetric metric
      WHERE metric.store.id = :storeId
        AND (:startMonth IS NULL OR metric.referenceMonth >= :startMonth)
        AND (:endMonth IS NULL OR metric.referenceMonth <= :endMonth)
      ORDER BY metric.referenceMonth ASC
      """)
  List<StoreFinancialMonthlyMetric> findByStoreIdAndReferenceMonthRange(
      @Param("storeId") Long storeId,
      @Param("startMonth") LocalDate startMonth,
      @Param("endMonth") LocalDate endMonth);

  @Override
  void deleteById(Long id);
}
