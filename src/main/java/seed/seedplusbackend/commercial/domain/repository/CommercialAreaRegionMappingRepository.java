package seed.seedplusbackend.commercial.domain.repository;

import java.util.List;
import java.util.Optional;
import seed.seedplusbackend.commercial.domain.entity.CommercialAreaRegionMapping;

public interface CommercialAreaRegionMappingRepository {

  <S extends CommercialAreaRegionMapping> S save(S entity);

  Optional<CommercialAreaRegionMapping> findById(Long id);

  List<CommercialAreaRegionMapping> findAll();

  List<CommercialAreaRegionMapping> findByCommercialArea_Id(Long commercialAreaId);

  List<CommercialAreaRegionMapping> findByRegion_Id(Long regionId);

  boolean existsById(Long id);

  void delete(CommercialAreaRegionMapping entity);

  void deleteById(Long id);

  long count();
}
