package seed.seedplusbackend.score.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import seed.seedplusbackend.region.domain.entity.Region;
import seed.seedplusbackend.region.infrastructure.repository.RegionJpaRepository;
import seed.seedplusbackend.score.domain.entity.ScoreComponentSnapshot;
import seed.seedplusbackend.score.domain.entity.ScoreComponentType;
import seed.seedplusbackend.score.domain.entity.ScoreGrade;
import seed.seedplusbackend.score.domain.entity.ScoreSnapshot;
import seed.seedplusbackend.score.domain.entity.ScoreTargetType;
import seed.seedplusbackend.score.domain.entity.ScoreType;
import seed.seedplusbackend.support.AbstractPostgresContainerTest;
import seed.seedplusbackend.support.RepositoryTest;
import seed.seedplusbackend.support.fixture.RegionFixture;

@RepositoryTest
@DisplayName("ScoreComponentSnapshotJpaRepository")
class ScoreComponentSnapshotJpaRepositoryTest extends AbstractPostgresContainerTest {

  @Autowired private ScoreComponentSnapshotJpaRepository repository;
  @Autowired private ScoreSnapshotJpaRepository scoreSnapshotJpaRepository;
  @Autowired private RegionJpaRepository regionJpaRepository;

  @Test
  @DisplayName("스코어 구성요소 스냅샷을 저장하고 조회할 수 있다")
  void saveAndFindById_smokeTest() {
    Region region = regionJpaRepository.save(RegionFixture.seoulGangnamYeoksamLegalDong());
    ScoreSnapshot snapshot =
        scoreSnapshotJpaRepository.save(
            ScoreSnapshot.builder()
                .targetType(ScoreTargetType.REGION)
                .store(null)
                .builderStore(null)
                .commercialArea(null)
                .region(region)
                .scoreType(ScoreType.RISK)
                .referenceMonth(LocalDate.of(2026, 5, 1))
                .totalScore(new BigDecimal("70.00"))
                .grade(ScoreGrade.C)
                .pd(null)
                .survivalProbability(null)
                .build());

    ScoreComponentSnapshot saved =
        repository.save(
            ScoreComponentSnapshot.builder()
                .scoreSnapshot(snapshot)
                .componentType(ScoreComponentType.OPERATING)
                .rawValue(new BigDecimal("12.3456"))
                .normalizedScore(new BigDecimal("75.50"))
                .weight(new BigDecimal("0.2500"))
                .contribution(new BigDecimal("18.8800"))
                .build());

    assertThat(saved.getId()).isNotNull();
    assertThat(repository.findById(saved.getId())).isPresent();
  }
}
