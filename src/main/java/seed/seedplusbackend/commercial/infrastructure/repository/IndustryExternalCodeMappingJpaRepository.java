package seed.seedplusbackend.commercial.infrastructure.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import seed.seedplusbackend.commercial.domain.entity.ExternalDataSource;
import seed.seedplusbackend.commercial.domain.entity.IndustryExternalCodeMapping;
import seed.seedplusbackend.commercial.domain.repository.IndustryExternalCodeMappingRepository;

public interface IndustryExternalCodeMappingJpaRepository
    extends JpaRepository<IndustryExternalCodeMapping, Long>,
        IndustryExternalCodeMappingRepository {

  @Override
  <S extends IndustryExternalCodeMapping> S save(S mapping);

  @Override
  List<IndustryExternalCodeMapping> findAllByIndustryIdAndSource(
      Long industryId, ExternalDataSource source);
}
