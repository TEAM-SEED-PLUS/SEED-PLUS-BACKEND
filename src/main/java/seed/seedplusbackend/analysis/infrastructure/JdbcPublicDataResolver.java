package seed.seedplusbackend.analysis.infrastructure;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import seed.seedplusbackend.analysis.application.port.PublicDataResolver;
import seed.seedplusbackend.analysis.application.result.PublicDataMetrics;

@Repository
@RequiredArgsConstructor
public class JdbcPublicDataResolver implements PublicDataResolver {

  private final JdbcTemplate jdbcTemplate;

  @Override
  public PublicDataMetrics resolve(String regionCode, String industryCode) {
    List<String> sources = new ArrayList<>();
    SalesMetrics sales = sales(regionCode, industryCode);
    Integer storeCount = storeCount(regionCode, industryCode);
    KosisMetrics kosis = kosis(industryCode);
    Integer traffic = traffic(regionCode);

    if (sales.available()) sources.add("서울시 상권분석서비스 추정매출");
    if (storeCount != null) sources.add("소상공인시장진흥공단 상가정보");
    if (kosis.available()) sources.add("KOSIS 기업생멸행정통계");
    if (traffic != null) sources.add("서울시 S-DoT 유동인구");

    boolean incomplete =
        !sales.available() || storeCount == null || !kosis.available() || traffic == null;
    return new PublicDataMetrics(
        sales.totalAmount(),
        storeCount,
        sales.districtAverage(),
        sales.cityAverage(),
        null,
        storeCount,
        null,
        storeCount,
        sales.growthRate(),
        storeCount,
        null,
        traffic,
        kosis.survivalRate(),
        kosis.closedBusinesses(),
        kosis.activeBusinesses(),
        kosis.newBusinesses(),
        incomplete,
        List.copyOf(sources));
  }

  private SalesMetrics sales(String regionCode, String industryCode) {
    List<SalesMetrics> rows =
        jdbcTemplate.query(
            """
            WITH mapped_areas AS (
              SELECT external_code
              FROM region_external_code_mappings
              WHERE region_code = ?
                AND source = 'SEOUL_ESTIMATED_SALES'
            ), mapped_industries AS (
              SELECT mapping.external_code
              FROM industry_external_code_mappings mapping
              JOIN industries industry ON industry.industry_id = mapping.industry_id
              WHERE industry.industry_code = ?
                AND mapping.source = 'SEOUL_ESTIMATED_SALES'
            ), quarter_sales AS (
              SELECT sales.stdr_yyqu_cd AS quarter,
                     SUM(sales.thsmon_selng_amt) AS total_amount,
                     COUNT(*) AS row_count
              FROM commercial_estimated_sales sales
              WHERE sales.trdar_cd IN (SELECT external_code FROM mapped_areas)
                AND sales.svc_induty_cd IN (SELECT external_code FROM mapped_industries)
              GROUP BY sales.stdr_yyqu_cd
            ), ranked_quarters AS (
              SELECT quarter, total_amount, row_count,
                     LAG(total_amount) OVER (ORDER BY quarter) AS previous_total
              FROM quarter_sales
            ), latest_district AS (
              SELECT quarter, total_amount, row_count, previous_total
              FROM ranked_quarters
              ORDER BY quarter DESC
              LIMIT 1
            ), latest_city AS (
              SELECT SUM(sales.thsmon_selng_amt) AS total_amount, COUNT(*) AS row_count
              FROM commercial_estimated_sales sales
              JOIN latest_district latest ON latest.quarter = sales.stdr_yyqu_cd
              WHERE sales.svc_induty_cd IN (SELECT external_code FROM mapped_industries)
            )
            SELECT district.total_amount,
                   ROUND(district.total_amount::numeric / NULLIF(district.row_count, 0), 0),
                   ROUND(city.total_amount::numeric / NULLIF(city.row_count, 0), 0),
                   ROUND(
                     (district.total_amount - district.previous_total)::numeric
                       / NULLIF(district.previous_total, 0) * 100,
                     3
                   )
            FROM latest_district district
            CROSS JOIN latest_city city
            """,
            (rs, rowNum) ->
                new SalesMetrics(
                    rs.getLong(1), rs.getBigDecimal(2), rs.getBigDecimal(3), rs.getBigDecimal(4)),
            regionCode,
            industryCode);
    return rows.isEmpty() ? SalesMetrics.empty() : rows.getFirst();
  }

  private Integer storeCount(String regionCode, String industryCode) {
    Integer count =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*)
            FROM small_business_stores
            WHERE legal_dong_code = ?
              AND ? IN (large_industry_code, medium_industry_code, small_industry_code)
            """,
            Integer.class,
            regionCode,
            industryCode);
    return count;
  }

  private KosisMetrics kosis(String industryCode) {
    List<KosisMetrics> rows =
        jdbcTemplate.query(
            """
            WITH count_codes AS (
              SELECT mapping.external_code
              FROM industry_external_code_mappings mapping
              JOIN industries industry ON industry.industry_id = mapping.industry_id
              WHERE industry.industry_code = ?
                AND mapping.source = 'KOSIS_BUSINESS_COUNT'
            ), survival_codes AS (
              SELECT mapping.external_code
              FROM industry_external_code_mappings mapping
              JOIN industries industry ON industry.industry_id = mapping.industry_id
              WHERE industry.industry_code = ?
                AND mapping.source = 'KOSIS_BUSINESS_SURVIVAL_RATE'
            ), latest_counts AS (
              SELECT SUM(active_business_count) AS active_business_count,
                     SUM(new_business_count) AS new_business_count,
                     SUM(closed_business_count) AS closed_business_count
              FROM kosis_business_rates
              WHERE reference_year = (
                SELECT MAX(reference_year)
                FROM kosis_business_rates
                WHERE industry_code IN (SELECT external_code FROM count_codes)
              )
                AND industry_code IN (SELECT external_code FROM count_codes)
            ), latest_survival AS (
              SELECT AVG(survival_rate) AS survival_rate
              FROM kosis_business_survival_rates
              WHERE reference_year = (
                SELECT MAX(reference_year)
                FROM kosis_business_survival_rates
                WHERE industry_code IN (SELECT external_code FROM survival_codes)
                  AND item_name ILIKE '%1년%'
              )
                AND industry_code IN (SELECT external_code FROM survival_codes)
                AND item_name ILIKE '%1년%'
            )
            SELECT survival.survival_rate,
                   counts.closed_business_count,
                   counts.active_business_count,
                   counts.new_business_count
            FROM latest_counts counts
            CROSS JOIN latest_survival survival
            """,
            (rs, rowNum) ->
                new KosisMetrics(
                    rs.getBigDecimal(1),
                    rs.getBigDecimal(2),
                    rs.getBigDecimal(3),
                    rs.getBigDecimal(4)),
            industryCode,
            industryCode);
    return rows.isEmpty() ? KosisMetrics.empty() : rows.getFirst();
  }

  private Integer traffic(String regionCode) {
    Long value =
        jdbcTemplate.queryForObject(
            """
            SELECT SUM(traffic.total_visitor_count)
            FROM seoul_sdot_district_traffic_metrics traffic
            JOIN regions region
              ON region.code = ?
             AND traffic.autonomous_district = region.sigungu
             AND traffic.administrative_district = region.dong
            """,
            Long.class,
            regionCode);
    return value == null || value <= 0 ? null : Math.toIntExact(value);
  }

  private record SalesMetrics(
      Long totalAmount, BigDecimal districtAverage, BigDecimal cityAverage, BigDecimal growthRate) {
    private static SalesMetrics empty() {
      return new SalesMetrics(null, null, null, null);
    }

    private boolean available() {
      return totalAmount != null;
    }
  }

  private record KosisMetrics(
      BigDecimal survivalRate,
      BigDecimal closedBusinesses,
      BigDecimal activeBusinesses,
      BigDecimal newBusinesses) {
    private static KosisMetrics empty() {
      return new KosisMetrics(null, null, null, null);
    }

    private boolean available() {
      return survivalRate != null || activeBusinesses != null;
    }
  }
}
