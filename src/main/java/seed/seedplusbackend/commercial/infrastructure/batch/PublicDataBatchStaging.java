package seed.seedplusbackend.commercial.infrastructure.batch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import seed.seedplusbackend.commercial.application.provider.CommercialDataType;

public class PublicDataBatchStaging {

  private final JdbcTemplate jdbc;
  private final ObjectMapper mapper;
  private final PublicDataBatchLease lease;
  private final PublicDataBatchPagePublisher publisher;
  private final TransactionTemplate transaction;
  private final ThreadLocal<Scope> active = new ThreadLocal<>();

  public PublicDataBatchStaging(
      JdbcTemplate jdbc,
      ObjectMapper mapper,
      PublicDataBatchLease lease,
      PublicDataBatchPagePublisher publisher,
      PlatformTransactionManager transactionManager) {
    this.jdbc = jdbc;
    this.mapper = mapper;
    this.lease = lease;
    this.publisher = publisher;
    transaction = new TransactionTemplate(transactionManager);
    transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
  }

  public Scope open(long executionId, CommercialDataType type, UUID token) {
    if (active.get() != null) throw new IllegalStateException("배치 수집 컨텍스트가 중복되었습니다.");
    transaction.executeWithoutResult(
        status -> {
          lease.renew(type, token);
          // 이전 실패/중단 실행의 페이지는 새 실행이 소유권을 획득한 뒤 정리한다.
          jdbc.update("DELETE FROM public_data_batch_pages WHERE data_type = ?", type.name());
        });
    Scope scope = new Scope(executionId, type, token);
    active.set(scope);
    return scope;
  }

  public boolean capture(CommercialDataType type, String target, List<?> rows) {
    Scope scope = active.get();
    if (scope == null) return false;
    if (scope.type != type) throw new IllegalStateException("배치 수집 데이터 유형이 일치하지 않습니다.");
    if (Thread.currentThread().isInterrupted()) throw new IllegalStateException("수집이 중단되었습니다.");
    if (rows == null || rows.isEmpty()) return true;
    final String payload;
    try {
      payload = mapper.writeValueAsString(rows);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("수집 데이터를 임시 저장할 수 없습니다.", exception);
    }
    transaction.executeWithoutResult(
        status -> {
          lease.renew(type, scope.token);
          jdbc.update(
              """
          INSERT INTO public_data_batch_pages(job_execution_id, data_type, storage_target, payload)
          VALUES (?, ?, ?, ?::jsonb)
          """,
              scope.executionId,
              type.name(),
              target,
              payload);
        });
    scope.count += rows.size();
    return true;
  }

  public long publish(long executionId, CommercialDataType type, UUID token) {
    return transaction.execute(
        status -> {
          lease.lockForPublication(type, token);
          long cursor = 0;
          long count = 0;
          while (true) {
            List<Page> pages =
                jdbc.query(
                    """
            SELECT page_id, storage_target, payload::text FROM public_data_batch_pages
            WHERE job_execution_id = ? AND data_type = ? AND page_id > ?
            ORDER BY page_id LIMIT 1
            """,
                    (rs, index) -> new Page(rs.getLong(1), rs.getString(2), rs.getString(3)),
                    executionId,
                    type.name(),
                    cursor);
            if (pages.isEmpty()) break;
            if (Thread.currentThread().isInterrupted())
              throw new IllegalStateException("반영이 중단되었습니다.");
            Page page = pages.getFirst();
            count += publisher.publish(type, page.target, page.payload);
            cursor = page.id;
          }
          if (count == 0) throw new IllegalStateException("반영할 데이터가 없습니다.");
          jdbc.update(
              """
          INSERT INTO public_data_batch_publications(data_type, job_execution_id, row_count, published_at)
          VALUES (?, ?, ?, clock_timestamp())
          ON CONFLICT (data_type) DO UPDATE SET
            job_execution_id = EXCLUDED.job_execution_id, row_count = EXCLUDED.row_count,
            published_at = EXCLUDED.published_at
          """,
              type.name(),
              executionId,
              count);
          discard(executionId);
          return count;
        });
  }

  public void discard(long executionId) {
    jdbc.update("DELETE FROM public_data_batch_pages WHERE job_execution_id = ?", executionId);
  }

  private record Page(long id, String target, String payload) {}

  public final class Scope implements AutoCloseable {
    private final long executionId;
    private final CommercialDataType type;
    private final UUID token;
    private long count;

    private Scope(long executionId, CommercialDataType type, UUID token) {
      this.executionId = executionId;
      this.type = type;
      this.token = token;
    }

    public long count() {
      return count;
    }

    @Override
    public void close() {
      active.remove();
    }
  }
}
