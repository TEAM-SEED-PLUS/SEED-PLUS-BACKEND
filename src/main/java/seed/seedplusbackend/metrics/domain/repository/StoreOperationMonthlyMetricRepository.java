package seed.seedplusbackend.metrics.domain.repository;

import java.util.List;
import java.util.Optional;
import seed.seedplusbackend.metrics.domain.entity.StoreOperationMonthlyMetric;

public interface StoreOperationMonthlyMetricRepository {

  <S extends StoreOperationMonthlyMetric> S save(S entity);

  Optional<StoreOperationMonthlyMetric> findById(Long id);

  List<StoreOperationMonthlyMetric> findAll();

  boolean existsById(Long id);

  void delete(StoreOperationMonthlyMetric entity);

  void deleteById(Long id);

  long count();
}
