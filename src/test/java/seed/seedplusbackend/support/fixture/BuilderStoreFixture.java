package seed.seedplusbackend.support.fixture;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStore;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreVisibilityStatus;
import seed.seedplusbackend.commercial.domain.entity.CommercialArea;
import seed.seedplusbackend.industry.domain.entity.Industry;
import seed.seedplusbackend.region.domain.entity.Region;
import seed.seedplusbackend.user.domain.entity.User;

public final class BuilderStoreFixture {

  private BuilderStoreFixture() {}

  public static BuilderStore publicBuilderStore(
      User owner, Region region, CommercialArea commercialArea, Industry industry) {
    return BuilderStore.builder()
        .user(owner)
        .region(region)
        .commercialArea(commercialArea)
        .industry(industry)
        .baseBuilding(null)
        .name("샘플 가상점포")
        .area(40)
        .expectedMonthlySales(50_000_000L)
        .expectedProfitRate(new BigDecimal("12.50"))
        .investmentPaybackMonths(36)
        .propertyScore(80)
        .monthlyRent(2_000_000L)
        .deposit(20_000_000L)
        .investmentAmount(100_000_000L)
        .description("테스트 가상점포")
        .likeCount(0L)
        .commentCount(0L)
        .visibilityStatus(BuilderStoreVisibilityStatus.PUBLIC)
        .uploadedAt(OffsetDateTime.now())
        .build();
  }
}
