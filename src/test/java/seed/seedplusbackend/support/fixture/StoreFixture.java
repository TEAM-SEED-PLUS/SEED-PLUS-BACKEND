package seed.seedplusbackend.support.fixture;

import seed.seedplusbackend.building.domain.entity.Building;
import seed.seedplusbackend.industry.domain.entity.Industry;
import seed.seedplusbackend.store.domain.entity.Store;
import seed.seedplusbackend.store.domain.entity.StoreStatus;

public final class StoreFixture {

  private StoreFixture() {}

  public static Store activeStore(Building building, Industry industry, String name) {
    return Store.builder()
        .building(building)
        .industry(industry)
        .floor("1F")
        .name(name)
        .code(null)
        .area(50)
        .deposit(10_000_000L)
        .monthlyRent(2_000_000L)
        .vacant(false)
        .status(StoreStatus.ACTIVE)
        .build();
  }
}
