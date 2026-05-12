package seed.seedplusbackend.metrics.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import seed.seedplusbackend.industry.domain.entity.Industry;
import seed.seedplusbackend.industry.infrastructure.repository.IndustryJpaRepository;
import seed.seedplusbackend.metrics.domain.entity.IndustryRegionMonthlyMetric;
import seed.seedplusbackend.region.domain.entity.Region;
import seed.seedplusbackend.region.infrastructure.repository.RegionJpaRepository;
import seed.seedplusbackend.support.AbstractPostgresContainerTest;
import seed.seedplusbackend.support.RepositoryTest;
import seed.seedplusbackend.support.fixture.IndustryFixture;
import seed.seedplusbackend.support.fixture.RegionFixture;

@RepositoryTest
@DisplayName("IndustryRegionMonthlyMetricJpaRepository")
class IndustryRegionMonthlyMetricJpaRepositoryTest extends AbstractPostgresContainerTest {

  @Autowired private IndustryRegionMonthlyMetricJpaRepository repository;
  @Autowired private RegionJpaRepository regionJpaRepository;
  @Autowired private IndustryJpaRepository industryJpaRepository;

  @Test
  @DisplayName("업종-지역 지표를 저장하고 조회할 수 있다")
  void saveAndFindById_smokeTest() {
    Region region = regionJpaRepository.save(RegionFixture.seoulGangnamYeoksamLegalDong());
    Industry industry = industryJpaRepository.save(IndustryFixture.largeRoot("IR-IND", "음식점업"));

    IndustryRegionMonthlyMetric saved =
        repository.save(
            IndustryRegionMonthlyMetric.builder()
                .region(region)
                .industry(industry)
                .referenceMonth(LocalDate.of(2026, 5, 1))
                .averageSales(40_000_000L)
                .salesChangeRate(new BigDecimal("5.00"))
                .averageCostRate(new BigDecimal("55.00"))
                .closureRate(new BigDecimal("3.00"))
                .storeCount(120L)
                .salesRank(5)
                .sourceName("KOSIS")
                .collectedAt(null)
                .build());

    assertThat(saved.getId()).isNotNull();
    assertThat(repository.findById(saved.getId())).isPresent();
  }
}
