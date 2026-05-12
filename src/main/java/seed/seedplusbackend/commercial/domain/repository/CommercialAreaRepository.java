package seed.seedplusbackend.commercial.domain.repository;

import java.util.List;
import java.util.Optional;
import seed.seedplusbackend.commercial.domain.entity.CommercialArea;

public interface CommercialAreaRepository {

  <S extends CommercialArea> S save(S entity);

  Optional<CommercialArea> findById(Long id);

  List<CommercialArea> findAll();

  boolean existsById(Long id);

  void delete(CommercialArea entity);

  void deleteById(Long id);

  long count();
}
