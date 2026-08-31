package seed.seedplusbackend.commercial.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import seed.seedplusbackend.commercial.application.port.CommercialDataCollectClaimPort;

@Repository
@RequiredArgsConstructor
public class CommercialDataCollectClaimJdbcAdapter implements CommercialDataCollectClaimPort {

  private static final String RESTART_COMPLETED_SQL =
      """
      UPDATE commercial_data_collect_histories
      SET status = 'RUNNING',
          total_count = 0,
          fetched_count = 0,
          last_start_index = 1,
          error_message = NULL,
          started_at = now(),
          finished_at = NULL,
          updated_at = now()
      WHERE commercial_data_collect_history_id = (
        SELECT completed.commercial_data_collect_history_id
        FROM commercial_data_collect_histories completed
        WHERE completed.data_type = ?
          AND completed.target_key = ?
          AND completed.status = 'COMPLETED'
          AND NOT EXISTS (
            SELECT 1
            FROM commercial_data_collect_histories running
            WHERE running.data_type = completed.data_type
              AND running.target_key = completed.target_key
              AND running.status = 'RUNNING'
          )
        ORDER BY completed.commercial_data_collect_history_id DESC
        FOR UPDATE SKIP LOCKED
        LIMIT 1
      )
      RETURNING commercial_data_collect_history_id
      """;

  private static final String INSERT_RUNNING_SQL =
      """
      INSERT INTO commercial_data_collect_histories (
        data_type,
        target_key,
        status,
        total_count,
        fetched_count,
        last_start_index,
        started_at,
        created_at
      )
      SELECT ?, ?, 'RUNNING', 0, 0, 1, now(), now()
      WHERE ?
         OR NOT EXISTS (
           SELECT 1
           FROM commercial_data_collect_histories completed
           WHERE completed.data_type = ?
             AND completed.target_key = ?
             AND completed.status = 'COMPLETED'
         )
      ON CONFLICT (data_type, target_key) WHERE status = 'RUNNING'
      DO NOTHING
      RETURNING commercial_data_collect_history_id
      """;

  private final JdbcTemplate jdbcTemplate;

  @Override
  @Transactional
  public Optional<Long> tryClaim(String dataType, String targetKey, boolean force) {
    if (force) {
      Optional<Long> restarted = queryId(RESTART_COMPLETED_SQL, dataType, targetKey);
      if (restarted.isPresent()) {
        return restarted;
      }
    }

    return queryId(INSERT_RUNNING_SQL, dataType, targetKey, force, dataType, targetKey);
  }

  private Optional<Long> queryId(String sql, Object... arguments) {
    List<Long> ids =
        jdbcTemplate.query(sql, (resultSet, rowNumber) -> resultSet.getLong(1), arguments);
    return ids.stream().findFirst();
  }
}
