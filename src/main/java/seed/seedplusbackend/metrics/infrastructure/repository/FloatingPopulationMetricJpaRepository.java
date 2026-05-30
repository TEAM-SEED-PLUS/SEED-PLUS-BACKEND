package seed.seedplusbackend.metrics.infrastructure.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import seed.seedplusbackend.metrics.domain.entity.FloatingPopulationMetric;
import seed.seedplusbackend.metrics.domain.repository.FloatingPopulationMetricRepository;

public interface FloatingPopulationMetricJpaRepository
    extends JpaRepository<FloatingPopulationMetric, Long>, FloatingPopulationMetricRepository {

  @Override
  <S extends FloatingPopulationMetric> S save(S entity);

  @Override
  Optional<FloatingPopulationMetric> findById(Long id);

  @Override
  void deleteById(Long id);
}
