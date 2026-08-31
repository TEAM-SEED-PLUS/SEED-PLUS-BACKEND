package seed.seedplusbackend.commercial.domain.repository;

import java.util.List;
import seed.seedplusbackend.commercial.domain.entity.ExternalDataSource;
import seed.seedplusbackend.commercial.domain.entity.RegionExternalCodeMapping;

public interface RegionExternalCodeMappingRepository {

  <S extends RegionExternalCodeMapping> S save(S mapping);

  List<RegionExternalCodeMapping> findAllByRegionCodeAndSource(
      String regionCode, ExternalDataSource source);
}
