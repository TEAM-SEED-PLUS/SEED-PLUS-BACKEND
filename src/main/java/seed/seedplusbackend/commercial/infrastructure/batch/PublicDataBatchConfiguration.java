package seed.seedplusbackend.commercial.infrastructure.batch;

import java.util.UUID;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import seed.seedplusbackend.commercial.application.provider.CommercialDataProviderRegistry;
import seed.seedplusbackend.commercial.application.provider.CommercialDataType;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "public-data.batch.enabled", havingValue = "true")
@Import({
  PublicDataBatchLease.class,
  PublicDataBatchStaging.class,
  PublicDataBatchPagePublisher.class,
  PublicDataBatchStoreRouting.class
})
public class PublicDataBatchConfiguration {

  public static final String JOB_NAME = "publicDataCollection";

  @Bean
  Job publicDataCollectionJob(
      JobRepository repository,
      PlatformTransactionManager transactionManager,
      PublicDataBatchCommandFactory commands,
      CommercialDataProviderRegistry providers,
      PublicDataBatchStaging staging,
      PublicDataBatchLease lease) {
    // 원격 API 대기 중에는 DB 트랜잭션을 열지 않는다. 임시 페이지 저장/최종 반영은 별도 트랜잭션.
    var noTransaction =
        new DefaultTransactionAttribute(TransactionDefinition.PROPAGATION_NOT_SUPPORTED);
    var collect =
        new StepBuilder("collectPublicData", repository)
            .tasklet(
                (contribution, context) -> {
                  JobExecution execution = contribution.getStepExecution().getJobExecution();
                  CommercialDataType type = type(execution);
                  UUID token = token(execution);
                  try (var scope = staging.open(execution.getId(), type, token)) {
                    for (var command : commands.create(type)) {
                      lease.renew(type, token);
                      long before = scope.count();
                      var progress = new PublicDataBatchProgress();
                      providers
                          .get(type)
                          .collect(
                              command,
                              (total, fetched, cursor) -> {
                                if (contribution.getStepExecution().isTerminateOnly()
                                    || Thread.currentThread().isInterrupted()) {
                                  throw new IllegalStateException("배치 수집 중단 요청을 받았습니다.");
                                }
                                lease.renew(type, token);
                                progress.update(total, fetched, cursor);
                              });
                      progress.verify(scope.count() - before);
                    }
                    contribution.incrementWriteCount(scope.count());
                  }
                  return RepeatStatus.FINISHED;
                },
                transactionManager)
            .transactionAttribute(noTransaction)
            .allowStartIfComplete(true)
            .build();
    var publish =
        new StepBuilder("publishPublicData", repository)
            .tasklet(
                (contribution, context) -> {
                  JobExecution execution = contribution.getStepExecution().getJobExecution();
                  contribution.incrementWriteCount(
                      staging.publish(execution.getId(), type(execution), token(execution)));
                  return RepeatStatus.FINISHED;
                },
                transactionManager)
            .transactionAttribute(noTransaction)
            .build();
    return new JobBuilder(JOB_NAME, repository).start(collect).next(publish).build();
  }

  @Bean
  PublicDataBatchLauncher publicDataBatchLauncher(
      Job publicDataCollectionJob,
      JobRepository repository,
      JobExplorer explorer,
      PublicDataBatchLease lease,
      PublicDataBatchStaging staging)
      throws Exception {
    var launcher = new TaskExecutorJobLauncher();
    launcher.setJobRepository(repository);
    // 실행 권한은 작업 종료까지 유지한다. 별도 비동기 실행기로 교체하지 않는다.
    launcher.setTaskExecutor(new SyncTaskExecutor());
    launcher.afterPropertiesSet();
    return new PublicDataBatchLauncher(
        publicDataCollectionJob, launcher, explorer, repository, lease, staging);
  }

  private static CommercialDataType type(JobExecution execution) {
    return CommercialDataType.valueOf(execution.getJobParameters().getString("source"));
  }

  private static UUID token(JobExecution execution) {
    return UUID.fromString(execution.getJobParameters().getString("ownerToken"));
  }
}
