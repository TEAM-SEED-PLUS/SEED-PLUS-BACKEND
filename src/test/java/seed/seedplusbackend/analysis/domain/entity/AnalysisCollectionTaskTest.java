package seed.seedplusbackend.analysis.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import seed.seedplusbackend.support.fixture.UserFixture;

@DisplayName("AnalysisCollectionTask")
class AnalysisCollectionTaskTest {

  @Test
  @DisplayName("실패한 작업만 다시 시작하며 시도 횟수를 누적한다")
  void restartFailedTask() {
    AnalysisCollectionTask task = createTask();

    task.start();
    task.fail("외부 API 일시 오류");
    task.start();

    assertThat(task.getStatus()).isEqualTo(AnalysisCollectionTaskStatus.RUNNING);
    assertThat(task.getAttemptCount()).isEqualTo(2);
    assertThat(task.getErrorMessage()).isNull();
  }

  @Test
  @DisplayName("완료한 작업은 다시 시작하지 않는다")
  void cannotRestartCompletedTask() {
    AnalysisCollectionTask task = createTask();
    task.start();
    task.complete();

    assertThatThrownBy(task::start).isInstanceOf(IllegalStateException.class);
    assertThat(task.getAttemptCount()).isEqualTo(1);
  }

  private AnalysisCollectionTask createTask() {
    AnalysisCollectionRun run =
        AnalysisCollectionRun.create(
            UserFixture.generalActiveUser("collection@test.com"),
            AnalysisCollectionType.PROFIT,
            "1168010100",
            "I101");
    return AnalysisCollectionTask.create(run, "ESTIMATED_SALES", "20262:1001495:I101");
  }
}
