package seed.seedplusbackend.industry.domain.repository;

import java.util.List;
import java.util.Optional;
import seed.seedplusbackend.industry.domain.entity.Industry;

public interface IndustryRepository {

  <S extends Industry> S save(S entity);

  Optional<Industry> findById(Long id);

  List<Industry> findAll();

  boolean existsById(Long id);

  void delete(Industry entity);

  void deleteById(Long id);

  long count();
}
