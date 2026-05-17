package seed.seedplusbackend.metrics.domain.repository;

import java.util.List;
import java.util.Optional;
import seed.seedplusbackend.metrics.domain.entity.CommercialAreaMonthlyMetric;

public interface CommercialAreaMonthlyMetricRepository {

  <S extends CommercialAreaMonthlyMetric> S save(S entity);

  Optional<CommercialAreaMonthlyMetric> findById(Long id);

  List<CommercialAreaMonthlyMetric> findAll();

  List<CommercialAreaMonthlyMetric> findByCommercialArea_IdOrderByReferenceMonthAsc(
      Long commercialAreaId);

  Optional<CommercialAreaMonthlyMetric> findFirstByCommercialArea_IdOrderByReferenceMonthDesc(
      Long commercialAreaId);

  boolean existsById(Long id);

  void delete(CommercialAreaMonthlyMetric entity);

  void deleteById(Long id);

  long count();
}
