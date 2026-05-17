package seed.seedplusbackend.region.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.region.domain.entity.Region;
import seed.seedplusbackend.region.domain.entity.RegionCodeType;
import seed.seedplusbackend.region.domain.repository.RegionRepository;

@Service
@RequiredArgsConstructor
public class RegionQueryService {

  private final RegionRepository regionRepository;

  @Transactional(readOnly = true)
  public List<Region> getRegions(String sido, String sigungu, RegionCodeType codeType) {
    return regionRepository.findAll().stream()
        .filter(region -> matches(region.getSido(), sido))
        .filter(region -> matches(region.getSigungu(), sigungu))
        .filter(region -> codeType == null || region.getCodeType() == codeType)
        .toList();
  }

  @Transactional(readOnly = true)
  public Region getRegion(Long regionId) {
    return regionRepository
        .findById(regionId)
        .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND_REGION));
  }

  private boolean matches(String value, String filter) {
    return !StringUtils.hasText(filter) || value.equals(filter);
  }
}
