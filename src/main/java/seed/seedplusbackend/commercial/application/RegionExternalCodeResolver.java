package seed.seedplusbackend.commercial.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import seed.seedplusbackend.commercial.domain.entity.ExternalDataSource;
import seed.seedplusbackend.commercial.domain.entity.RegionExternalCodeMapping;
import seed.seedplusbackend.commercial.domain.repository.RegionExternalCodeMappingRepository;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.region.application.RegionResolver;

@Component
@RequiredArgsConstructor
public class RegionExternalCodeResolver {

  private final RegionResolver regionResolver;
  private final RegionExternalCodeMappingRepository externalCodeMappingRepository;

  @Transactional(readOnly = true)
  public List<String> resolve(String regionCode, ExternalDataSource source) {
    regionResolver.resolveLegalDong(regionCode);
    List<String> externalCodes =
        externalCodeMappingRepository.findAllByRegionCodeAndSource(regionCode, source).stream()
            .map(RegionExternalCodeMapping::getExternalCode)
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
