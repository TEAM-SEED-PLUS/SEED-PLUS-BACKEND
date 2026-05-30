package seed.seedplusbackend.metrics.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import seed.seedplusbackend.commercial.domain.entity.CommercialArea;
import seed.seedplusbackend.commercial.infrastructure.repository.CommercialAreaJpaRepository;
import seed.seedplusbackend.metrics.domain.entity.CommercialAreaMonthlyMetric;
import seed.seedplusbackend.support.AbstractPostgresContainerTest;
import seed.seedplusbackend.support.RepositoryTest;
import seed.seedplusbackend.support.fixture.CommercialAreaFixture;

@RepositoryTest
@DisplayName("CommercialAreaMonthlyMetricJpaRepository")
class CommercialAreaMonthlyMetricJpaRepositoryTest extends AbstractPostgresContainerTest {

  @Autowired private CommercialAreaMonthlyMetricJpaRepository repository;
  @Autowired private CommercialAreaJpaRepository commercialAreaJpaRepository;

  @Test
  @DisplayName("상권 월간 지표를 저장하고 조회할 수 있다")
  void saveAndFindById_smokeTest() {
    CommercialArea area =
        commercialAreaJpaRepository.save(CommercialAreaFixture.developedActive("CA상권"));

    CommercialAreaMonthlyMetric saved =
        repository.save(
            CommercialAreaMonthlyMetric.builder()
                .commercialArea(area)
                .referenceMonth(LocalDate.of(2026, 5, 1))
                .floatingPopulation(100_000L)
                .vacancyRate(new BigDecimal("5.00"))
                .averageRent(3_000_000L)
                .openingRate(new BigDecimal("2.50"))
                .closureRate(new BigDecimal("1.50"))
                .salesGrowthRate(new BigDecimal("4.00"))
                .competitionDensity(new BigDecimal("12.50"))
                .activityScore(75)
                .sourceName("SBA")
                .collectedAt(null)
                .build());

    assertThat(saved.getId()).isNotNull();
    assertThat(repository.findById(saved.getId())).isPresent();
  }
}
