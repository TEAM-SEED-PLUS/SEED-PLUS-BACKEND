package seed.seedplusbackend.commercial.infrastructure.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import seed.seedplusbackend.commercial.domain.entity.CommercialArea;
import seed.seedplusbackend.commercial.domain.repository.CommercialAreaRepository;

public interface CommercialAreaJpaRepository
    extends JpaRepository<CommercialArea, Long>, CommercialAreaRepository {

  @Override
  <S extends CommercialArea> S save(S entity);

  @Override
  Optional<CommercialArea> findById(Long id);

  @Override
  void deleteById(Long id);
}
