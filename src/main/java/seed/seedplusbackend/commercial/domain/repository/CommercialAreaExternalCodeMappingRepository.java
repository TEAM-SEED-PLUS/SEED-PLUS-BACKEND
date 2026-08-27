package seed.seedplusbackend.commercial.domain.repository;

import java.util.List;
import seed.seedplusbackend.commercial.domain.entity.CommercialAreaExternalCodeMapping;
import seed.seedplusbackend.commercial.domain.entity.ExternalDataSource;

public interface CommercialAreaExternalCodeMappingRepository {

  <S extends CommercialAreaExternalCodeMapping> S save(S mapping);

  List<CommercialAreaExternalCodeMapping> findAllByCommercialAreaIdAndSource(
      Long commercialAreaId, ExternalDataSource source);

  List<CommercialAreaExternalCodeMapping> findAllByRegionIdAndSource(
      Long regionId, ExternalDataSource source);
}
