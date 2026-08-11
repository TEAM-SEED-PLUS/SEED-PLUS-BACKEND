package seed.seedplusbackend.commercial.infrastructure.repository;

import java.sql.Types;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import seed.seedplusbackend.commercial.application.port.SeoulSdotFootTrafficStorePort;
import seed.seedplusbackend.commercial.application.result.SeoulSdotFootTrafficRowResult;

@Repository
@RequiredArgsConstructor
public class SeoulSdotFootTrafficJdbcRepository implements SeoulSdotFootTrafficStorePort {

  private static final String UPSERT_SQL =
      """
      INSERT INTO seoul_sdot_foot_traffic (
        model_name, serial_number, sensing_time, region_type,
        autonomous_district, administrative_district, visitor_count,
        registered_at, collected_at, created_at, updated_at
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, now(), now(), now())
      ON CONFLICT (serial_number, sensing_time)
      DO UPDATE SET
        model_name = EXCLUDED.model_name,
        region_type = EXCLUDED.region_type,
        autonomous_district = EXCLUDED.autonomous_district,
        administrative_district = EXCLUDED.administrative_district,
        visitor_count = EXCLUDED.visitor_count,
        registered_at = EXCLUDED.registered_at,
        collected_at = now(),
        updated_at = now()
      """;

  private final JdbcTemplate jdbcTemplate;

  @Override
  @Transactional
  public void upsertAll(List<SeoulSdotFootTrafficRowResult> rows) {
    if (rows == null || rows.isEmpty()) {
      return;
    }
    jdbcTemplate.batchUpdate(
        UPSERT_SQL,
        rows,
        500,
        (statement, row) -> {
          statement.setString(1, row.modelName());
          statement.setString(2, row.serialNumber());
          statement.setObject(3, row.sensingTime());
          statement.setString(4, row.regionType());
          statement.setString(5, row.autonomousDistrict());
          statement.setString(6, row.administrativeDistrict());
          statement.setLong(7, row.visitorCount());
          if (row.registeredAt() == null) {
            statement.setNull(8, Types.TIMESTAMP);
          } else {
            statement.setObject(8, row.registeredAt());
          }
        });
  }
}
