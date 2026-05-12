package seed.seedplusbackend.metrics.infrastructure.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import seed.seedplusbackend.metrics.domain.entity.CommercialAreaMonthlyMetric;
import seed.seedplusbackend.metrics.domain.repository.CommercialAreaMonthlyMetricRepository;

public interface CommercialAreaMonthlyMetricJpaRepository
    extends JpaRepository<CommercialAreaMonthlyMetric, Long>,
        CommercialAreaMonthlyMetricRepository {

  @Override
  <S extends CommercialAreaMonthlyMetric> S save(S entity);

  @Override
  Optional<CommercialAreaMonthlyMetric> findById(Long id);

  @Override
  void deleteById(Long id);
}
