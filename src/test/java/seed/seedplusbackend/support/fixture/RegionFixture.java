package seed.seedplusbackend.support.fixture;

import seed.seedplusbackend.region.domain.entity.Region;
import seed.seedplusbackend.region.domain.entity.RegionCodeType;

public final class RegionFixture {

  private static final String TEST_LEGAL_DONG_CODE = "9999999998";

  private RegionFixture() {}

  public static Region seoulGangnamYeoksamLegalDong() {
    return Region.builder()
        .sido("서울특별시")
        .sigungu("강남구")
        .dong("역삼동")
        .code(TEST_LEGAL_DONG_CODE)
        .codeType(RegionCodeType.LEGAL_DONG)
        .build();
  }
}
