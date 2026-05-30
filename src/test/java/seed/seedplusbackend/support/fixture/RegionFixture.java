package seed.seedplusbackend.support.fixture;

import seed.seedplusbackend.region.domain.entity.Region;
import seed.seedplusbackend.region.domain.entity.RegionCodeType;

public final class RegionFixture {

  private RegionFixture() {}

  public static Region seoulGangnamYeoksamLegalDong() {
    return Region.builder()
        .sido("서울특별시")
        .sigungu("강남구")
        .dong("역삼동")
        .code("1168010100")
        .codeType(RegionCodeType.LEGAL_DONG)
        .build();
  }
}
