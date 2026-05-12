package seed.seedplusbackend.metrics.infrastructure.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
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
  void deleteById(Long id);
}
