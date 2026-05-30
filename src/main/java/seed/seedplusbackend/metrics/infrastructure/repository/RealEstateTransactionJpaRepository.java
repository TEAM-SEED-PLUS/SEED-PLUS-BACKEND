package seed.seedplusbackend.metrics.infrastructure.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import seed.seedplusbackend.metrics.domain.entity.RealEstateTransaction;
import seed.seedplusbackend.metrics.domain.repository.RealEstateTransactionRepository;

public interface RealEstateTransactionJpaRepository
    extends JpaRepository<RealEstateTransaction, Long>, RealEstateTransactionRepository {

  @Override
  <S extends RealEstateTransaction> S save(S entity);

  @Override
  Optional<RealEstateTransaction> findById(Long id);

  @Override
  void deleteById(Long id);
}
