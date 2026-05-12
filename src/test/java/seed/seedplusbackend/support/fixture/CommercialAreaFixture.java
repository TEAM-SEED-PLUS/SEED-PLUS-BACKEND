package seed.seedplusbackend.support.fixture;

import seed.seedplusbackend.commercial.domain.entity.CommercialArea;
import seed.seedplusbackend.commercial.domain.entity.CommercialAreaStatus;
import seed.seedplusbackend.commercial.domain.entity.CommercialAreaType;

public final class CommercialAreaFixture {

  private CommercialAreaFixture() {}

  public static CommercialArea developedActive(String name) {
    return CommercialArea.builder()
        .name(name)
        .type(CommercialAreaType.DEVELOPED)
        .description("개발된 상권")
        .status(CommercialAreaStatus.ACTIVE)
        .build();
  }
}
