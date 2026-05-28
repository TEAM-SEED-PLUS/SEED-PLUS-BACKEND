package seed.seedplusbackend.region.application;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.region.domain.entity.Region;
import seed.seedplusbackend.region.domain.entity.RegionCodeType;
import seed.seedplusbackend.region.domain.repository.RegionRepository;

@Service
@RequiredArgsConstructor
public class RegionResolver {

  private final RegionRepository regionRepository;

  @Transactional(readOnly = true)
  public Region resolveLegalDong(String regionCode) {
    return regionRepository
        .findByCodeAndCodeType(regionCode, RegionCodeType.LEGAL_DONG)
        .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND_REGION));
  }

  @Transactional(readOnly = true)
  public String resolveLegalDongName(String regionCode) {
    return toRegionName(resolveLegalDong(regionCode));
  }

  private String toRegionName(Region region) {
    return Stream.of(region.getSido(), region.getSigungu(), region.getDong())
        .filter(value -> value != null && !value.isBlank())
        .collect(Collectors.joining(" "));
  }
}
