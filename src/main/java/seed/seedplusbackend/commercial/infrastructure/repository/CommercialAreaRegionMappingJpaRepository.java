package seed.seedplusbackend.commercial.infrastructure.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import seed.seedplusbackend.commercial.domain.entity.CommercialAreaRegionMapping;
import seed.seedplusbackend.commercial.domain.repository.CommercialAreaRegionMappingRepository;

public interface CommercialAreaRegionMappingJpaRepository
    extends JpaRepository<CommercialAreaRegionMapping, Long>,
        CommercialAreaRegionMappingRepository {

  @Override
  <S extends CommercialAreaRegionMapping> S save(S entity);

  @Override
  Optional<CommercialAreaRegionMapping> findById(Long id);

  @Override
  void deleteById(Long id);
}
