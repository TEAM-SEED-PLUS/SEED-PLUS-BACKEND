package seed.seedplusbackend.commercial.infrastructure.repository;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import seed.seedplusbackend.commercial.application.port.KosisBusinessCountStorePort;
import seed.seedplusbackend.commercial.application.result.KosisBusinessCountRowResult;

@Repository
@RequiredArgsConstructor
public class KosisBusinessCountJdbcRepository implements KosisBusinessCountStorePort {

  private static final String UPSERT_SQL =
      """
      INSERT INTO kosis_business_counts (
        organization_id, table_id, table_name,
        industry_code, industry_name, classification_name,
        item_id, item_name, unit_name, period_type,
        reference_year, business_count, source_updated_at,
        collected_at, created_at, updated_at
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now(), now(), now())
      ON CONFLICT (table_id, industry_code, item_id, reference_year)
      DO UPDATE SET
        organization_id = EXCLUDED.organization_id,
        table_name = EXCLUDED.table_name,
        industry_name = EXCLUDED.industry_name,
        classification_name = EXCLUDED.classification_name,
        item_name = EXCLUDED.item_name,
        unit_name = EXCLUDED.unit_name,
        period_type = EXCLUDED.period_type,
        business_count = EXCLUDED.business_count,
        source_updated_at = EXCLUDED.source_updated_at,
        collected_at = now(),
        updated_at = now()
      """;

  private final JdbcTemplate jdbcTemplate;

  @Override
  @Transactional
  public void upsertAll(List<KosisBusinessCountRowResult> rows) {
    if (rows == null || rows.isEmpty()) {
      return;
    }

    jdbcTemplate.batchUpdate(
        UPSERT_SQL,
        rows,
        500,
        (statement, row) -> {
          int index = 1;
          statement.setString(index++, row.organizationId());
          statement.setString(index++, row.tableId());
          statement.setString(index++, row.tableName());
          statement.setString(index++, row.industryCode());
          statement.setString(index++, row.industryName());
          statement.setString(index++, row.classificationName());
          statement.setString(index++, row.itemId());
          statement.setString(index++, row.itemName());
          statement.setString(index++, row.unitName());
          statement.setString(index++, row.periodType());
          statement.setInt(index++, row.referenceYear());
          statement.setBigDecimal(index++, row.businessCount());
          statement.setString(index, row.sourceUpdatedAt());
        });
  }
}
