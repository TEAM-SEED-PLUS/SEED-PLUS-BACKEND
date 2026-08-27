package seed.seedplusbackend.commercial.infrastructure.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

  @Override
  @Query(
      """
      SELECT externalMapping
      FROM CommercialAreaExternalCodeMapping externalMapping
      JOIN CommercialAreaRegionMapping regionMapping
        ON regionMapping.commercialArea = externalMapping.commercialArea
      WHERE regionMapping.region.id = :regionId
        AND externalMapping.source = :source
      """)
  List<CommercialAreaExternalCodeMapping> findAllByRegionIdAndSource(
      @Param("regionId") Long regionId, @Param("source") ExternalDataSource source);
}
