package seed.seedplusbackend.commercial.infrastructure.batch;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import seed.seedplusbackend.commercial.application.provider.CommercialDataType;

@Slf4j
@RequiredArgsConstructor
public class PublicDataBatchLauncher {

  private final Job job;
  private final JobLauncher launcher;
  private final JobExplorer explorer;
  private final JobRepository repository;
  private final PublicDataBatchLease lease;
  private final PublicDataBatchStaging staging;

  // 같은 source + slot은 같은 JobInstance다. 실패한 슬롯을 전달하면 처음부터 안전하게 재수집한다.
  public Optional<JobExecution> launch(CommercialDataType type, Instant slot) throws Exception {
    if (type == null || type == CommercialDataType.REB_SMALL_RETAIL_RENT || slot == null) {
      throw new IllegalArgumentException("배치 데이터 유형과 실행 슬롯을 확인해주세요.");
    }
    UUID token = UUID.randomUUID();
    if (!lease.acquire(type, token)) return Optional.empty();
    JobExecution execution = null;
    try {
      recoverInterruptedExecutions(type);
      var parameters =
          new JobParametersBuilder()
              .addString("source", type.name())
              .addString("slot", slot.truncatedTo(ChronoUnit.MINUTES).toString())
              .addString("ownerToken", token.toString(), false)
              .toJobParameters();
      try {
        execution = launcher.run(job, parameters);
      } catch (JobInstanceAlreadyCompleteException alreadyCompleted) {
        execution = repository.getLastJobExecution(job.getName(), parameters);
      }
      return Optional.ofNullable(execution);
    } finally {
      try {
        if (execution != null) staging.discard(execution.getId());
      } catch (RuntimeException exception) {
        log.warn("배치 임시 데이터 정리 실패 source={}", type, exception);
      } finally {
        lease.release(type, token);
      }
    }
  }

  private void recoverInterruptedExecutions(CommercialDataType type) {
    // 새 실행 권한을 얻은 뒤에만 복구한다. 이전 실행은 ownerToken 검증 때문에 반영할 수 없다.
    for (JobExecution running : explorer.findRunningJobExecutions(job.getName())) {
      if (!type.name().equals(running.getJobParameters().getString("source"))) continue;
      JobExecution stale = explorer.getJobExecution(running.getId());
      if (stale == null) continue;
      for (var step : stale.getStepExecutions()) {
        if (step.getStatus().isRunning()) {
          step.setStatus(BatchStatus.FAILED);
          step.setExitStatus(ExitStatus.FAILED.addExitDescription("수집 실행 권한 만료 후 복구"));
          step.setEndTime(LocalDateTime.now());
          repository.update(step);
        }
      }
      stale.setStatus(BatchStatus.FAILED);
      stale.setExitStatus(ExitStatus.FAILED.addExitDescription("수집 실행 권한 만료 후 복구"));
      stale.setEndTime(LocalDateTime.now());
      repository.update(stale);
      staging.discard(stale.getId());
    }
  }
}
