package seed.seedplusbackend.metrics.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import seed.seedplusbackend.building.domain.entity.Building;
import seed.seedplusbackend.building.infrastructure.repository.BuildingJpaRepository;
import seed.seedplusbackend.commercial.domain.entity.CommercialArea;
import seed.seedplusbackend.commercial.infrastructure.repository.CommercialAreaJpaRepository;
import seed.seedplusbackend.industry.domain.entity.Industry;
import seed.seedplusbackend.industry.infrastructure.repository.IndustryJpaRepository;
import seed.seedplusbackend.metrics.domain.entity.StoreOperationMonthlyMetric;
import seed.seedplusbackend.region.domain.entity.Region;
import seed.seedplusbackend.region.infrastructure.repository.RegionJpaRepository;
import seed.seedplusbackend.store.domain.entity.Store;
import seed.seedplusbackend.store.infrastructure.repository.StoreJpaRepository;
import seed.seedplusbackend.support.AbstractPostgresContainerTest;
import seed.seedplusbackend.support.RepositoryTest;
import seed.seedplusbackend.support.fixture.BuildingFixture;
import seed.seedplusbackend.support.fixture.CommercialAreaFixture;
import seed.seedplusbackend.support.fixture.IndustryFixture;
import seed.seedplusbackend.support.fixture.RegionFixture;
import seed.seedplusbackend.support.fixture.StoreFixture;

@RepositoryTest
@DisplayName("StoreOperationMonthlyMetricJpaRepository")
class StoreOperationMonthlyMetricJpaRepositoryTest extends AbstractPostgresContainerTest {

  @Autowired private StoreOperationMonthlyMetricJpaRepository repository;
  @Autowired private StoreJpaRepository storeJpaRepository;
  @Autowired private BuildingJpaRepository buildingJpaRepository;
  @Autowired private RegionJpaRepository regionJpaRepository;
  @Autowired private CommercialAreaJpaRepository commercialAreaJpaRepository;
  @Autowired private IndustryJpaRepository industryJpaRepository;

  @Test
  @DisplayName("점포 운영 지표를 저장하고 조회할 수 있다")
  void saveAndFindById_smokeTest() {
    Region region = regionJpaRepository.save(RegionFixture.seoulGangnamYeoksamLegalDong());
    CommercialArea area =
        commercialAreaJpaRepository.save(CommercialAreaFixture.developedActive("OP상권"));
    Building building =
        buildingJpaRepository.save(BuildingFixture.seoulGangnamBuilding(region, area));
    Industry industry = industryJpaRepository.save(IndustryFixture.largeRoot("OP-IND", "음식점업"));
    Store store = storeJpaRepository.save(StoreFixture.activeStore(building, industry, "운영점포"));

    StoreOperationMonthlyMetric saved =
        repository.save(
            StoreOperationMonthlyMetric.builder()
                .store(store)
                .referenceMonth(LocalDate.of(2026, 5, 1))
                .monthlySales(50_000_000L)
                .averageDailySales(1_500_000L)
                .customerCount(2_500L)
                .averageOrderAmount(20_000L)
                .revisitRate(new BigDecimal("35.50"))
                .tableTurnoverRate(new BigDecimal("3.20"))
                .deliverySalesRatio(new BigDecimal("12.30"))
                .build());

    assertThat(saved.getId()).isNotNull();
    assertThat(repository.findById(saved.getId())).isPresent();
  }
}
