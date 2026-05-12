package seed.seedplusbackend.metrics.domain.repository;

import java.util.List;
import java.util.Optional;
import seed.seedplusbackend.metrics.domain.entity.FloatingPopulationMetric;

public interface FloatingPopulationMetricRepository {

  <S extends FloatingPopulationMetric> S save(S entity);

  Optional<FloatingPopulationMetric> findById(Long id);

  List<FloatingPopulationMetric> findAll();

  boolean existsById(Long id);

  void delete(FloatingPopulationMetric entity);

  void deleteById(Long id);

  long count();
}
