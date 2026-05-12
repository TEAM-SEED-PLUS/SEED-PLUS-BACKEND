package seed.seedplusbackend.metrics.domain.repository;

import java.util.List;
import java.util.Optional;
import seed.seedplusbackend.metrics.domain.entity.RealEstateTransaction;

public interface RealEstateTransactionRepository {

  <S extends RealEstateTransaction> S save(S entity);

  Optional<RealEstateTransaction> findById(Long id);

  List<RealEstateTransaction> findAll();

  boolean existsById(Long id);

  void delete(RealEstateTransaction entity);

  void deleteById(Long id);

  long count();
}
