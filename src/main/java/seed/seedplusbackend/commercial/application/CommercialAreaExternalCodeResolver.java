package seed.seedplusbackend.commercial.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import seed.seedplusbackend.commercial.domain.entity.CommercialAreaExternalCodeMapping;
import seed.seedplusbackend.commercial.domain.entity.ExternalDataSource;
import seed.seedplusbackend.commercial.domain.repository.CommercialAreaExternalCodeMappingRepository;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.region.application.RegionResolver;
import seed.seedplusbackend.region.domain.entity.Region;

@Component
@RequiredArgsConstructor
public class CommercialAreaExternalCodeResolver {

  private final RegionResolver regionResolver;
  private final CommercialAreaExternalCodeMappingRepository externalCodeMappingRepository;

  @Transactional(readOnly = true)
  public List<String> resolve(String regionCode, ExternalDataSource source) {
    Region region = regionResolver.resolveLegalDong(regionCode);
    List<String> externalCodes =
        externalCodeMappingRepository.findAllByRegionIdAndSource(region.getId(), source).stream()
            .map(CommercialAreaExternalCodeMapping::getExternalCode)
            .distinct()
            .sorted()
            .toList();

    if (externalCodes.isEmpty()) {
      throw new ApplicationException(
          ErrorCode.NOT_FOUND_COMMERCIAL_AREA,
          "regionCode=%s, source=%s".formatted(regionCode, source),
          null);
    }

    return externalCodes;
  }
}
