package seed.seedplusbackend.industry.domain.repository;

import java.util.List;
import java.util.Optional;
import seed.seedplusbackend.industry.domain.entity.Industry;
import seed.seedplusbackend.industry.domain.entity.IndustryStatus;

public interface IndustryRepository {

  <S extends Industry> S save(S entity);

  Optional<Industry> findById(Long id);

  Optional<Industry> findByIdAndStatus(Long id, IndustryStatus status);

  List<Industry> findAll();

  List<Industry> findByStatusOrderByIdAsc(IndustryStatus status);

  boolean existsById(Long id);

  void delete(Industry entity);

  void deleteById(Long id);

  long count();
}
