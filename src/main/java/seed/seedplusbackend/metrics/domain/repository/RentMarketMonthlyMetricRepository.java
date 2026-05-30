package seed.seedplusbackend.metrics.domain.repository;

import java.util.List;
import java.util.Optional;
import seed.seedplusbackend.metrics.domain.entity.RentMarketMonthlyMetric;

public interface RentMarketMonthlyMetricRepository {

  <S extends RentMarketMonthlyMetric> S save(S entity);

  Optional<RentMarketMonthlyMetric> findById(Long id);

  List<RentMarketMonthlyMetric> findAll();

  boolean existsById(Long id);

  void delete(RentMarketMonthlyMetric entity);

  void deleteById(Long id);

  long count();
}
