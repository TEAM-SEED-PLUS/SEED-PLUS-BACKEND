package seed.seedplusbackend.score.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import seed.seedplusbackend.region.domain.entity.Region;
import seed.seedplusbackend.region.infrastructure.repository.RegionJpaRepository;
import seed.seedplusbackend.score.domain.entity.ScoreGrade;
import seed.seedplusbackend.score.domain.entity.ScoreSnapshot;
import seed.seedplusbackend.score.domain.entity.ScoreTargetType;
import seed.seedplusbackend.score.domain.entity.ScoreType;
import seed.seedplusbackend.support.AbstractPostgresContainerTest;
import seed.seedplusbackend.support.RepositoryTest;
import seed.seedplusbackend.support.fixture.RegionFixture;

@RepositoryTest
@DisplayName("ScoreSnapshotJpaRepository")
class ScoreSnapshotJpaRepositoryTest extends AbstractPostgresContainerTest {

  @Autowired private ScoreSnapshotJpaRepository repository;
  @Autowired private RegionJpaRepository regionJpaRepository;

  @Test
  @DisplayName("REGION 타깃의 스코어 스냅샷을 저장하고 조회할 수 있다")
  void saveAndFindById_smokeTest_regionTarget() {
    Region region = regionJpaRepository.save(RegionFixture.seoulGangnamYeoksamLegalDong());

    ScoreSnapshot saved =
        repository.save(
            ScoreSnapshot.builder()
                .targetType(ScoreTargetType.REGION)
                .store(null)
                .builderStore(null)
                .commercialArea(null)
                .region(region)
                .scoreType(ScoreType.PROPERTY)
                .referenceMonth(LocalDate.of(2026, 5, 1))
                .totalScore(new BigDecimal("82.50"))
                .grade(ScoreGrade.B)
                .pd(new BigDecimal("3.50"))
                .survivalProbability(new BigDecimal("96.50"))
                .build());

    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getCreatedAt()).isNotNull();
    assertThat(repository.findById(saved.getId())).isPresent();
  }
}
