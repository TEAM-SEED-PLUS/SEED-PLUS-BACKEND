package seed.seedplusbackend.commercial.infrastructure.repository;

import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import seed.seedplusbackend.commercial.application.port.RebSmallRetailRentStorePort;
import seed.seedplusbackend.commercial.application.result.RebSmallRetailRentFileResult;
import seed.seedplusbackend.commercial.application.result.RebSmallRetailRentRowResult;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;

@Repository
@RequiredArgsConstructor
public class RebSmallRetailRentJdbcRepository implements RebSmallRetailRentStorePort {

  private final JdbcTemplate jdbcTemplate;

  @Override
  @Transactional
  public void replace(
      String sourceFileName, String sourceFileHash, RebSmallRetailRentFileResult file) {
    try {
      upsertAreas(file.rows());
      insertMetrics(sourceFileName, sourceFileHash, file.rows());
    } catch (DataAccessException exception) {
      throw new ApplicationException(
          ErrorCode.REB_RENT_FILE_IMPORT_FAILED, "database write failed", exception);
    }
  }

  private void upsertAreas(List<RebSmallRetailRentRowResult> rows) {
    Map<String, RebSmallRetailRentRowResult> uniqueAreas = new LinkedHashMap<>();
    for (RebSmallRetailRentRowResult row : rows) {
      uniqueAreas.putIfAbsent(row.sourceAreaKey(), row);
    }

    String sql =
        """
        INSERT INTO reb_small_retail_rent_areas (
          source_area_key, source_row_number, area_name, area_path, area_level, updated_at
        ) VALUES (?, ?, ?, ?, ?, now())
        ON CONFLICT (source_area_key) DO UPDATE SET
          source_row_number = EXCLUDED.source_row_number,
          area_name = EXCLUDED.area_name,
          area_path = EXCLUDED.area_path,
          area_level = EXCLUDED.area_level,
          updated_at = now()
        """;

    jdbcTemplate.batchUpdate(
        sql,
        uniqueAreas.values(),
        500,
        (statement, row) -> {
          statement.setString(1, row.sourceAreaKey());
          statement.setInt(2, row.sourceRowNumber());
          statement.setString(3, row.areaName());
          statement.setString(4, row.areaPath());
          statement.setInt(5, row.areaLevel());
        });
  }

  private void insertMetrics(
      String sourceFileName, String sourceFileHash, List<RebSmallRetailRentRowResult> rows) {
    String sql =
        """
        INSERT INTO reb_small_retail_rent_metrics (
          reb_small_retail_rent_area_id,
          reference_year,
          reference_quarter,
          rent_per_square_meter_thousand_krw,
          source_file_name,
          source_file_hash,
          collected_at,
          updated_at
        )
        SELECT reb_small_retail_rent_area_id, ?, ?, ?, ?, ?, now(), now()
        FROM reb_small_retail_rent_areas
        WHERE source_area_key = ?
        ON CONFLICT (
          reb_small_retail_rent_area_id, reference_year, reference_quarter
        ) DO UPDATE SET
          rent_per_square_meter_thousand_krw =
            EXCLUDED.rent_per_square_meter_thousand_krw,
          source_file_name = EXCLUDED.source_file_name,
          source_file_hash = EXCLUDED.source_file_hash,
          collected_at = now(),
          updated_at = now()
        """;

    jdbcTemplate.batchUpdate(
        sql,
        rows,
        500,
        (statement, row) -> {
          statement.setInt(1, row.referenceYear());
          statement.setInt(2, row.referenceQuarter());
          statement.setObject(3, row.rentPerSquareMeterThousandKrw(), Types.DECIMAL);
          statement.setString(4, sourceFileName);
          statement.setString(5, sourceFileHash);
          statement.setString(6, row.sourceAreaKey());
        });
  }
}
