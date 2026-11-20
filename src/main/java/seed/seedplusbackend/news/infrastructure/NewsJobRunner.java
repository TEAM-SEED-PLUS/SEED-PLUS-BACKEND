package seed.seedplusbackend.news.infrastructure;

import java.time.*;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import seed.seedplusbackend.global.error.*;
import seed.seedplusbackend.news.domain.NewsPolicy;

@RequiredArgsConstructor
public class NewsJobRunner {
  private final Job job;
  private final JobLauncher launcher;
  private final Clock clock;

  public JobExecution run(String operation) {
    if (!Set.of("MSS_PRESS", "MSS_ANNOUNCEMENT", "INSIGHTS", "DIGEST").contains(operation))
      throw new ApplicationException(ErrorCode.INVALID_PARAMETER);
    try {
      JobExecution execution =
          launcher.run(
              job,
              new JobParametersBuilder()
                  .addString("operation", operation)
                  .addString("runId", UUID.randomUUID().toString())
                  .addString("date", LocalDate.now(clock.withZone(NewsPolicy.ZONE)).toString())
                  .toJobParameters());
      if (execution.getStatus() != BatchStatus.COMPLETED)
        throw new ApplicationException(ErrorCode.NEWS_SOURCE_FAILED);
      return execution;
    } catch (ApplicationException e) {
      throw e;
    } catch (Exception e) {
      throw new ApplicationException(ErrorCode.NEWS_SOURCE_FAILED, e);
    }
  }
}
