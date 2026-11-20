package seed.seedplusbackend.news.infrastructure;

import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.*;
import org.springframework.transaction.support.TransactionTemplate;
import seed.seedplusbackend.global.error.*;
import seed.seedplusbackend.news.application.port.NewsJobLockPort;

@Repository
public class JdbcNewsJobLock implements NewsJobLockPort {
  private final JdbcTemplate jdbc;
  private final TransactionTemplate transaction;

  public JdbcNewsJobLock(JdbcTemplate jdbc, PlatformTransactionManager manager) {
    this.jdbc = jdbc;
    transaction = new TransactionTemplate(manager);
    transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
  }

  @Override
  public boolean acquire(String job, UUID owner) {
    return !jdbc.queryForList(
            """
     INSERT INTO news_job_leases(job_key,owner_token,lease_until)
     VALUES (?,?,clock_timestamp()+interval '15 minutes')
     ON CONFLICT(job_key) DO UPDATE SET owner_token=excluded.owner_token,lease_until=excluded.lease_until
     WHERE news_job_leases.lease_until<clock_timestamp() RETURNING job_key
     """,
            String.class,
            job,
            owner)
        .isEmpty();
  }

  @Override
  public void owned(String job, UUID owner, Runnable work) {
    transaction.executeWithoutResult(
        status -> {
          var locked =
              jdbc.queryForList(
                  """
       SELECT job_key FROM news_job_leases WHERE job_key=? AND owner_token=? AND lease_until>clock_timestamp() FOR UPDATE
       """,
                  String.class,
                  job,
                  owner);
          if (locked.isEmpty()) throw new ApplicationException(ErrorCode.NEWS_LEASE_LOST);
          jdbc.update(
              "UPDATE news_job_leases SET lease_until=clock_timestamp()+interval '15 minutes' WHERE job_key=? AND owner_token=?",
              job,
              owner);
          work.run();
        });
  }

  @Override
  public void release(String job, UUID owner) {
    jdbc.update("DELETE FROM news_job_leases WHERE job_key=? AND owner_token=?", job, owner);
  }
}
