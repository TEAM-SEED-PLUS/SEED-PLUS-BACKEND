package seed.seedplusbackend.metrics.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import seed.seedplusbackend.commercial.domain.entity.CommercialArea;
import seed.seedplusbackend.commercial.infrastructure.repository.CommercialAreaJpaRepository;
import seed.seedplusbackend.metrics.domain.entity.RentMarketMonthlyMetric;
import seed.seedplusbackend.support.AbstractPostgresContainerTest;
import seed.seedplusbackend.support.RepositoryTest;
import seed.seedplusbackend.support.fixture.CommercialAreaFixture;

@RepositoryTest
@DisplayName("RentMarketMonthlyMetricJpaRepository")
class RentMarketMonthlyMetricJpaRepositoryTest extends AbstractPostgresContainerTest {

  @Autowired private RentMarketMonthlyMetricJpaRepository repository;
  @Autowired private CommercialAreaJpaRepository commercialAreaJpaRepository;

  @Test
  @DisplayName("임대시장 월간 지표를 저장하고 조회할 수 있다")
  void saveAndFindById_smokeTest_commercialAreaTarget() {
    CommercialArea area =
        commercialAreaJpaRepository.save(CommercialAreaFixture.developedActive("RM상권"));

    RentMarketMonthlyMetric saved =
        repository.save(
            RentMarketMonthlyMetric.builder()
                .region(null)
                .commercialArea(area)
                .referenceMonth(LocalDate.of(2026, 5, 1))
                .averageRentPerArea(120_000L)
                .averageDeposit(20_000_000L)
                .vacancyRate(new BigDecimal("4.50"))
                .rentChangeRate(new BigDecimal("2.00"))
                .sourceName("KAB")
                .collectedAt(null)
                .build());

    assertThat(saved.getId()).isNotNull();
    assertThat(repository.findById(saved.getId())).isPresent();
  }
}
