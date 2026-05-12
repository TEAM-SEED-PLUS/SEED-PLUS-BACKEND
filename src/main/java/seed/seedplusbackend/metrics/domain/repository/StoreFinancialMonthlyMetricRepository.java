package seed.seedplusbackend.metrics.domain.repository;

import java.util.List;
import java.util.Optional;
import seed.seedplusbackend.metrics.domain.entity.StoreFinancialMonthlyMetric;

public interface StoreFinancialMonthlyMetricRepository {

  <S extends StoreFinancialMonthlyMetric> S save(S entity);

  Optional<StoreFinancialMonthlyMetric> findById(Long id);

  List<StoreFinancialMonthlyMetric> findAll();

  boolean existsById(Long id);

  void delete(StoreFinancialMonthlyMetric entity);

  void deleteById(Long id);

  long count();
}
