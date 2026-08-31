package seed.seedplusbackend.commercial.infrastructure.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import seed.seedplusbackend.commercial.domain.entity.ExternalDataSource;
import seed.seedplusbackend.commercial.domain.entity.RegionExternalCodeMapping;
import seed.seedplusbackend.commercial.domain.repository.RegionExternalCodeMappingRepository;

public interface RegionExternalCodeMappingJpaRepository
    extends JpaRepository<RegionExternalCodeMapping, Long>, RegionExternalCodeMappingRepository {

  @Override
  <S extends RegionExternalCodeMapping> S save(S mapping);

  @Override
  List<RegionExternalCodeMapping> findAllByRegionCodeAndSource(
      String regionCode, ExternalDataSource source);
}
