package seed.seedplusbackend.commercial.domain.repository;

import java.util.List;
import seed.seedplusbackend.commercial.domain.entity.ExternalDataSource;
import seed.seedplusbackend.commercial.domain.entity.IndustryExternalCodeMapping;

public interface IndustryExternalCodeMappingRepository {

  <S extends IndustryExternalCodeMapping> S save(S mapping);

  List<IndustryExternalCodeMapping> findAllByIndustryIdAndSource(
      Long industryId, ExternalDataSource source);
}
