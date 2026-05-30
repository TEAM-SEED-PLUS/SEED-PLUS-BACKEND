package seed.seedplusbackend.metrics.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import seed.seedplusbackend.metrics.domain.entity.FloatingPopulationGender;
import seed.seedplusbackend.metrics.domain.entity.FloatingPopulationMetric;
import seed.seedplusbackend.region.domain.entity.Region;
import seed.seedplusbackend.region.infrastructure.repository.RegionJpaRepository;
import seed.seedplusbackend.support.AbstractPostgresContainerTest;
import seed.seedplusbackend.support.RepositoryTest;
import seed.seedplusbackend.support.fixture.RegionFixture;

@RepositoryTest
@DisplayName("FloatingPopulationMetricJpaRepository")
class FloatingPopulationMetricJpaRepositoryTest extends AbstractPostgresContainerTest {

  @Autowired private FloatingPopulationMetricJpaRepository repository;
  @Autowired private RegionJpaRepository regionJpaRepository;

  @Test
  @DisplayName("region 기반의 유동인구 지표를 저장하고 조회할 수 있다")
  void saveAndFindById_smokeTest_regionTarget() {
    Region region = regionJpaRepository.save(RegionFixture.seoulGangnamYeoksamLegalDong());

    FloatingPopulationMetric saved =
        repository.save(
            FloatingPopulationMetric.builder()
                .region(region)
                .commercialArea(null)
                .referenceDate(LocalDate.of(2026, 5, 1))
                .timeSlotStart(LocalTime.of(12, 0))
                .ageGroup("20s")
                .gender(FloatingPopulationGender.ALL)
                .populationCount(5_000L)
                .sourceName("SKT")
                .collectedAt(null)
                .build());

    assertThat(saved.getId()).isNotNull();
    assertThat(repository.findById(saved.getId())).isPresent();
  }
}
