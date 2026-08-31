package seed.seedplusbackend.analysis.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import seed.seedplusbackend.analysis.domain.entity.AnalysisCollectionRun;
import seed.seedplusbackend.analysis.domain.entity.AnalysisCollectionTask;
import seed.seedplusbackend.analysis.domain.entity.AnalysisCollectionTaskStatus;
import seed.seedplusbackend.analysis.domain.entity.AnalysisCollectionType;
import seed.seedplusbackend.support.AbstractPostgresContainerTest;
import seed.seedplusbackend.support.RepositoryTest;
import seed.seedplusbackend.support.fixture.UserFixture;
import seed.seedplusbackend.user.domain.entity.User;
import seed.seedplusbackend.user.infrastructure.repository.UserJpaRepository;

@RepositoryTest
@DisplayName("AnalysisCollectionTaskJpaRepository")
class AnalysisCollectionTaskJpaRepositoryTest extends AbstractPostgresContainerTest {

  @Autowired private AnalysisCollectionRunJpaRepository runRepository;
  @Autowired private AnalysisCollectionTaskJpaRepository taskRepository;
  @Autowired private UserJpaRepository userRepository;

  @Test
  @DisplayName("한 계산 요청에서 실패한 수집 작업만 조회한다")
  void findFailedTasksByRun() {
    User user = userRepository.save(UserFixture.generalActiveUser("task-repository@test.com"));
    AnalysisCollectionRun run =
        runRepository.save(
            AnalysisCollectionRun.create(
                user, AnalysisCollectionType.SURVIVAL, "1168010100", "I21201"));
    AnalysisCollectionTask completed =
        AnalysisCollectionTask.create(run, "ESTIMATED_SALES", "20262:1001495:I21201");
    completed.start();
    completed.complete();
    AnalysisCollectionTask failed =
        AnalysisCollectionTask.create(run, "KOSIS_BUSINESS_SURVIVAL", "2025:I21201");
    failed.start();
    failed.fail("timeout");
    taskRepository.save(completed);
    taskRepository.save(failed);

    assertThat(
            taskRepository.findAllByRunIdAndStatus(
                run.getId(), AnalysisCollectionTaskStatus.FAILED))
        .containsExactly(failed);
  }
}
