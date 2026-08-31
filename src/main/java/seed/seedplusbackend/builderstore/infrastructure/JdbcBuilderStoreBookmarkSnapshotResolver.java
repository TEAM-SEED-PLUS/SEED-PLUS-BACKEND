package seed.seedplusbackend.builderstore.infrastructure;

import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import seed.seedplusbackend.builderstore.application.port.BuilderStoreBookmarkSnapshotResolver;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreBookmarkSnapshot;

@Repository
@RequiredArgsConstructor
public class JdbcBuilderStoreBookmarkSnapshotResolver
    implements BuilderStoreBookmarkSnapshotResolver {

  private final JdbcTemplate jdbcTemplate;

  @Override
  public BuilderStoreBookmarkSnapshot resolve(
      String regionCode, String industryCode, Long regionId, Long commercialAreaId) {
    return jdbcTemplate.queryForObject(
        """
        WITH sales_area_codes AS (
          SELECT external_code
          FROM region_external_code_mappings
          WHERE region_code = ?
            AND source = 'SEOUL_ESTIMATED_SALES'
        ), sales_industry_codes AS (
          SELECT mapping.external_code
          FROM industry_external_code_mappings mapping
          JOIN industries industry ON industry.industry_id = mapping.industry_id
          WHERE industry.industry_code = ?
            AND mapping.source = 'SEOUL_ESTIMATED_SALES'
        ), latest_sales AS (
          SELECT sales.stdr_yyqu_cd, CAST(SUM(sales.thsmon_selng_amt) AS BIGINT) AS sales_amount
          FROM commercial_estimated_sales sales
          WHERE sales.trdar_cd IN (SELECT external_code FROM sales_area_codes)
            AND sales.svc_induty_cd IN (SELECT external_code FROM sales_industry_codes)
          GROUP BY sales.stdr_yyqu_cd
          ORDER BY sales.stdr_yyqu_cd DESC
          LIMIT 1
        ), store_snapshot AS (
          SELECT MAX(collected_at) AS collected_at, COUNT(*)::INTEGER AS store_count
          FROM small_business_stores
          WHERE legal_dong_code = ?
            AND ? IN (large_industry_code, medium_industry_code, small_industry_code)
        ), survival_codes AS (
          SELECT mapping.external_code
          FROM industry_external_code_mappings mapping
          JOIN industries industry ON industry.industry_id = mapping.industry_id
          WHERE industry.industry_code = ?
            AND mapping.source = 'KOSIS_BUSINESS_SURVIVAL_RATE'
        ), latest_survival AS (
          SELECT reference_year, AVG(survival_rate) AS survival_rate
          FROM kosis_business_survival_rates
          WHERE industry_code IN (SELECT external_code FROM survival_codes)
            AND item_name ILIKE '%1년%'
          GROUP BY reference_year
          ORDER BY reference_year DESC
          LIMIT 1
        ), count_codes AS (
          SELECT mapping.external_code
          FROM industry_external_code_mappings mapping
          JOIN industries industry ON industry.industry_id = mapping.industry_id
          WHERE industry.industry_code = ?
            AND mapping.source = 'KOSIS_BUSINESS_COUNT'
        ), latest_counts AS (
          SELECT reference_year,
                 SUM(active_business_count) AS active_business_count,
                 SUM(new_business_count) AS new_business_count,
                 SUM(closed_business_count) AS closed_business_count
          FROM kosis_business_rates
          WHERE industry_code IN (SELECT external_code FROM count_codes)
          GROUP BY reference_year
          ORDER BY reference_year DESC
          LIMIT 1
        ), latest_rent AS (
          SELECT metric.reference_year,
                 metric.reference_quarter,
                 AVG(metric.rent_per_square_meter_thousand_krw) AS rent_amount
          FROM reb_small_retail_rent_metrics metric
          JOIN reb_small_retail_rent_areas area
            ON area.reb_small_retail_rent_area_id = metric.reb_small_retail_rent_area_id
          WHERE area.commercial_area_id = ?
             OR (area.commercial_area_id IS NULL AND area.region_id = ?)
          GROUP BY metric.reference_year, metric.reference_quarter
          ORDER BY metric.reference_year DESC, metric.reference_quarter DESC
          LIMIT 1
        )
        SELECT sales.stdr_yyqu_cd,
               sales.sales_amount,
               survival.reference_year,
               survival.survival_rate,
               counts.reference_year,
               counts.active_business_count,
               counts.new_business_count,
               counts.closed_business_count,
               stores.collected_at,
               stores.store_count,
               rent.reference_year,
               rent.reference_quarter,
               rent.rent_amount
        FROM (SELECT 1) seed
        LEFT JOIN latest_sales sales ON TRUE
        LEFT JOIN latest_survival survival ON TRUE
        LEFT JOIN latest_counts counts ON TRUE
        LEFT JOIN store_snapshot stores ON TRUE
        LEFT JOIN latest_rent rent ON TRUE
        """,
        (rs, rowNum) ->
            new BuilderStoreBookmarkSnapshot(
                rs.getString(1),
                rs.getObject(2, Long.class),
                rs.getObject(3, Integer.class),
                rs.getBigDecimal(4),
                rs.getObject(5, Integer.class),
                rs.getBigDecimal(6),
                rs.getBigDecimal(7),
                rs.getBigDecimal(8),
                rs.getObject(9, OffsetDateTime.class),
                rs.getObject(10, Integer.class),
                rs.getObject(11, Integer.class),
                rs.getObject(12, Integer.class),
                rs.getBigDecimal(13)),
        regionCode,
        industryCode,
        regionCode,
        industryCode,
        industryCode,
        industryCode,
        commercialAreaId,
        regionId);
  }
}
