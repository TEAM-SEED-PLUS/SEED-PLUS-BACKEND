package seed.seedplusbackend.news.infrastructure;

import java.time.*;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.*;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.transaction.*;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import seed.seedplusbackend.news.application.*;
import seed.seedplusbackend.news.domain.*;

@Configuration(proxyBeanMethods = false)
public class NewsConfiguration {
  @Bean
  Clock newsClock() {
    return Clock.systemUTC();
  }

  @Bean
  Job newsPipelineJob(
      JobRepository repository,
      PlatformTransactionManager manager,
      NewsCollectionService collection,
      NewsMaintenanceService maintenance) {
    var noTransaction = new DefaultTransactionAttribute();
    noTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_NOT_SUPPORTED);
    var step =
        new StepBuilder("processNews", repository)
            .tasklet(
                (contribution, context) -> {
                  var parameters =
                      contribution.getStepExecution().getJobExecution().getJobParameters();
                  String operation = parameters.getString("operation");
                  if ("INSIGHTS".equals(operation)) maintenance.insights();
                  else if ("DIGEST".equals(operation))
                    maintenance.digest(LocalDate.parse(parameters.getString("date")));
                  else collection.collect(NewsSource.valueOf(operation));
                  return RepeatStatus.FINISHED;
                },
                manager)
            .transactionAttribute(noTransaction)
            .build();
    return new JobBuilder("newsPipeline", repository).start(step).build();
  }

  @Bean
  NewsJobRunner newsJobRunner(
      @Qualifier("newsPipelineJob") Job job,
      JobRepository repository,
      @Qualifier("newsClock") Clock clock)
      throws Exception {
    var launcher = new TaskExecutorJobLauncher();
    launcher.setJobRepository(repository);
    launcher.setTaskExecutor(new SyncTaskExecutor());
    launcher.afterPropertiesSet();
    return new NewsJobRunner(job, launcher, clock);
  }

  @Bean
  ApplicationRunner newsRunOnce(
      NewsJobRunner runner, @Value("${news.batch.run-once:}") String operation) {
    return args -> {
      if (!operation.isBlank()) runner.run(operation);
    };
  }
}
