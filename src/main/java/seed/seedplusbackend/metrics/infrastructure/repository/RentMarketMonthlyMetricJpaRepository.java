package seed.seedplusbackend.metrics.infrastructure.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import seed.seedplusbackend.metrics.domain.entity.RentMarketMonthlyMetric;
import seed.seedplusbackend.metrics.domain.repository.RentMarketMonthlyMetricRepository;

public interface RentMarketMonthlyMetricJpaRepository
    extends JpaRepository<RentMarketMonthlyMetric, Long>, RentMarketMonthlyMetricRepository {

  @Override
  <S extends RentMarketMonthlyMetric> S save(S entity);

  @Override
  Optional<RentMarketMonthlyMetric> findById(Long id);

  @Override
  void deleteById(Long id);
}
