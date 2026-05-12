package seed.seedplusbackend.metrics.infrastructure.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
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
  void deleteById(Long id);
}
