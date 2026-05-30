package seed.seedplusbackend.metrics.domain.repository;

import java.util.List;
import java.util.Optional;
import seed.seedplusbackend.metrics.domain.entity.IndustryRegionMonthlyMetric;

public interface IndustryRegionMonthlyMetricRepository {

  <S extends IndustryRegionMonthlyMetric> S save(S entity);

  Optional<IndustryRegionMonthlyMetric> findById(Long id);

  List<IndustryRegionMonthlyMetric> findAll();

  boolean existsById(Long id);

  void delete(IndustryRegionMonthlyMetric entity);

  void deleteById(Long id);

  long count();
}
