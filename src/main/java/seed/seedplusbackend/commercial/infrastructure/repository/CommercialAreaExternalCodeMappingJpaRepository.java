package seed.seedplusbackend.commercial.infrastructure.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import seed.seedplusbackend.commercial.domain.entity.CommercialAreaExternalCodeMapping;
import seed.seedplusbackend.commercial.domain.entity.ExternalDataSource;
import seed.seedplusbackend.commercial.domain.repository.CommercialAreaExternalCodeMappingRepository;

public interface CommercialAreaExternalCodeMappingJpaRepository
    extends JpaRepository<CommercialAreaExternalCodeMapping, Long>,
        CommercialAreaExternalCodeMappingRepository {

  @Override
  <S extends CommercialAreaExternalCodeMapping> S save(S mapping);

  @Override
  List<CommercialAreaExternalCodeMapping> findAllByCommercialAreaIdAndSource(
      Long commercialAreaId, ExternalDataSource source);
}
