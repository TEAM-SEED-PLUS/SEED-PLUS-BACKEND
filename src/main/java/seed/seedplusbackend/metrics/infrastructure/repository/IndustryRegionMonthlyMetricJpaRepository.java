package seed.seedplusbackend.metrics.infrastructure.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import seed.seedplusbackend.metrics.domain.entity.IndustryRegionMonthlyMetric;
import seed.seedplusbackend.metrics.domain.repository.IndustryRegionMonthlyMetricRepository;

public interface IndustryRegionMonthlyMetricJpaRepository
    extends JpaRepository<IndustryRegionMonthlyMetric, Long>,
        IndustryRegionMonthlyMetricRepository {

  @Override
  <S extends IndustryRegionMonthlyMetric> S save(S entity);

  @Override
  Optional<IndustryRegionMonthlyMetric> findById(Long id);

  @Override
  void deleteById(Long id);
}
