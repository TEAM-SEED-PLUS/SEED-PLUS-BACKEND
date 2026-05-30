package seed.seedplusbackend.commercial.domain.repository;

import java.util.List;
import java.util.Optional;
import seed.seedplusbackend.commercial.domain.entity.CommercialArea;
import seed.seedplusbackend.commercial.domain.entity.CommercialAreaStatus;

public interface CommercialAreaRepository {

  <S extends CommercialArea> S save(S entity);

  Optional<CommercialArea> findById(Long id);

  Optional<CommercialArea> findByIdAndStatusNot(Long id, CommercialAreaStatus status);

  List<CommercialArea> findAll();

  List<CommercialArea> findByStatusOrderByIdAsc(CommercialAreaStatus status);

  boolean existsById(Long id);

  void delete(CommercialArea entity);

  void deleteById(Long id);

  long count();
}
