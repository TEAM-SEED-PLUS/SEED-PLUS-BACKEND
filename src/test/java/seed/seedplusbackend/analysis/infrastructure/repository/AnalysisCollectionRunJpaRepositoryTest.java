package seed.seedplusbackend.analysis.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import seed.seedplusbackend.analysis.domain.entity.AnalysisCollectionRun;
import seed.seedplusbackend.analysis.domain.entity.AnalysisCollectionType;
import seed.seedplusbackend.support.AbstractPostgresContainerTest;
import seed.seedplusbackend.support.RepositoryTest;
import seed.seedplusbackend.support.fixture.UserFixture;
import seed.seedplusbackend.user.domain.entity.User;
import seed.seedplusbackend.user.infrastructure.repository.UserJpaRepository;

@RepositoryTest
@DisplayName("AnalysisCollectionRunJpaRepository")
class AnalysisCollectionRunJpaRepositoryTest extends AbstractPostgresContainerTest {

  @Autowired private AnalysisCollectionRunJpaRepository runRepository;
  @Autowired private UserJpaRepository userRepository;

  @Test
  @DisplayName("수집 실행은 소유한 사용자 ID로만 조회한다")
  void findsRunOnlyForOwner() {
    User owner = userRepository.save(UserFixture.generalActiveUser("run-owner@test.com"));
    User other = userRepository.save(UserFixture.generalActiveUser("run-other@test.com"));
    AnalysisCollectionRun run =
        runRepository.save(
            AnalysisCollectionRun.create(
                owner, AnalysisCollectionType.PROFIT, "1168010100", "G22199"));

    assertThat(runRepository.findByIdAndUserId(run.getId(), owner.getId())).contains(run);
    assertThat(runRepository.findByIdAndUserId(run.getId(), other.getId())).isEmpty();
  }
}
