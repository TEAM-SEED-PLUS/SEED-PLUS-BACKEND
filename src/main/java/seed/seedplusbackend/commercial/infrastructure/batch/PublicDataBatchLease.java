package seed.seedplusbackend.commercial.infrastructure.batch;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import seed.seedplusbackend.commercial.application.provider.CommercialDataType;

@RequiredArgsConstructor
public class PublicDataBatchLease {

  private final JdbcTemplate jdbc;

  public boolean acquire(CommercialDataType type, UUID token) {
    return !jdbc.queryForList(
            """
            INSERT INTO public_data_batch_leases(data_type, owner_token, lease_until)
            VALUES (?, ?, clock_timestamp() + interval '15 minutes')
            ON CONFLICT (data_type) DO UPDATE
            SET owner_token = EXCLUDED.owner_token, lease_until = EXCLUDED.lease_until
            WHERE public_data_batch_leases.lease_until < clock_timestamp()
            RETURNING data_type
            """,
            String.class,
            type.name(),
            token)
        .isEmpty();
  }

  public void renew(CommercialDataType type, UUID token) {
    int updated =
        jdbc.update(
            """
        UPDATE public_data_batch_leases
        SET lease_until = clock_timestamp() + interval '15 minutes'
        WHERE data_type = ? AND owner_token = ? AND lease_until > clock_timestamp()
        """,
            type.name(),
            token);
    if (updated != 1) {
      throw new IllegalStateException("배치 실행 권한이 만료되거나 다른 실행으로 변경되었습니다.");
    }
  }

  // 반드시 반영 트랜잭션 안에서 호출한다. 새 실행이 권한을 획득하는 것도 커밋까지 대기한다.
  public void lockForPublication(CommercialDataType type, UUID token) {
    if (jdbc.queryForList(
            """
        SELECT data_type FROM public_data_batch_leases
        WHERE data_type = ? AND owner_token = ? AND lease_until > clock_timestamp()
        FOR UPDATE
        """,
            String.class,
            type.name(),
            token)
        .isEmpty()) {
      throw new IllegalStateException("실행 권한이 없는 배치는 데이터를 반영할 수 없습니다.");
    }
  }

  public void release(CommercialDataType type, UUID token) {
    jdbc.update(
        "DELETE FROM public_data_batch_leases WHERE data_type = ? AND owner_token = ?",
        type.name(),
        token);
  }
}
