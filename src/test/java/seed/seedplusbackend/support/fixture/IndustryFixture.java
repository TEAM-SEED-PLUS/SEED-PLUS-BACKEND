package seed.seedplusbackend.support.fixture;

import seed.seedplusbackend.industry.domain.entity.Industry;
import seed.seedplusbackend.industry.domain.entity.IndustryLevel;
import seed.seedplusbackend.industry.domain.entity.IndustryStatus;

public final class IndustryFixture {

  private IndustryFixture() {}

  public static Industry largeRoot(String code, String name) {
    return Industry.builder()
        .industryCode(code)
        .name(name)
        .parentIndustry(null)
        .level(IndustryLevel.LARGE)
        .status(IndustryStatus.ACTIVE)
        .build();
  }

  public static Industry mediumChild(String code, String name, Industry parent) {
    return Industry.builder()
        .industryCode(code)
        .name(name)
        .parentIndustry(parent)
        .level(IndustryLevel.MEDIUM)
        .status(IndustryStatus.ACTIVE)
        .build();
  }
}
