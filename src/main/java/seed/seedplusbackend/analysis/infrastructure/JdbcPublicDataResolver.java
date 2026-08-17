package seed.seedplusbackend.analysis.infrastructure;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import seed.seedplusbackend.analysis.application.port.PublicDataResolver;
import seed.seedplusbackend.analysis.application.result.PublicDataMetrics;

@Repository
@RequiredArgsConstructor
public class JdbcPublicDataResolver implements PublicDataResolver {

  private final JdbcTemplate jdbcTemplate;

  @Override
  public PublicDataMetrics resolve(String regionName, String industryName) {
    MvpPublicDataMapping.Mapping mapping = MvpPublicDataMapping.resolve(regionName, industryName);
    List<String> sources = new ArrayList<>();
    boolean fallbackUsed = false;

    SalesMetrics sales = sales(mapping);
    if (sales.available()) sources.add("서울시 상권분석서비스 추정매출");
    else fallbackUsed = true;

    Integer storeCount = storeCount(mapping);
    if (storeCount != null) sources.add("소상공인시장진흥공단 상가정보");
    else fallbackUsed = true;

    KosisMetrics kosis = kosis(mapping);
    if (kosis.available()) sources.add("KOSIS 기업생멸행정통계");
    else fallbackUsed = true;

    Integer traffic = traffic(mapping);
    if (traffic != null) sources.add("서울시 실시간 인구데이터");
    else {
      traffic = CalculatorFallback.TRAFFIC_INDEX;
      fallbackUsed = true;
    }

    int areaStoreCount = storeCount == null ? CalculatorFallback.STORE_LIST_IN_AREA : storeCount;
    return new PublicDataMetrics(
        sales.totalAmount(),
        storeCount,
        sales.districtAverage(),
        sales.cityAverage(),
        CalculatorFallback.STORE_ZONE_ONE,
        areaStoreCount,
        CalculatorFallback.STORE_LIST_IN_RADIUS,
        storeCount,
        sales.growthRate() == null
            ? BigDecimal.valueOf(CalculatorFallback.SALES_GROWTH_RATE)
            : sales.growthRate(),
        storeCount == null ? CalculatorFallback.STORE_DENSITY : storeCount,
        BigDecimal.valueOf(CalculatorFallback.VACANCY_RATE),
        traffic,
        kosis.survivalRate(),
        kosis.closedBusinesses(),
        kosis.activeBusinesses(),
        kosis.newBusinesses(),
        true,
        List.copyOf(sources));
  }

  private SalesMetrics sales(MvpPublicDataMapping.Mapping mapping) {
    try {
      List<SalesRow> rows =
          jdbcTemplate.query(
              """
              WITH latest AS (
                SELECT MAX(stdr_yyqu_cd) AS quarter FROM commercial_estimated_sales
              )
              SELECT trdar_cd_nm, thsmon_selng_amt
              FROM commercial_estimated_sales, latest
              WHERE stdr_yyqu_cd = latest.quarter
                AND svc_induty_cd_nm ILIKE ?
              """,
              (rs, rowNum) -> new SalesRow(rs.getString(1), rs.getLong(2)),
              like(mapping.industryKeyword()));
      if (rows.isEmpty()) return SalesMetrics.empty();
      long cityTotal = rows.stream().mapToLong(SalesRow::amount).sum();
      List<SalesRow> districtRows =
          rows.stream()
              .filter(
                  row ->
                      row.areaName() != null && row.areaName().contains(mapping.districtKeyword()))
              .toList();
      long selectedTotal =
          districtRows.isEmpty()
              ? cityTotal
              : districtRows.stream().mapToLong(SalesRow::amount).sum();
      BigDecimal cityAverage = average(cityTotal, rows.size());
      BigDecimal districtAverage =
          districtRows.isEmpty() ? cityAverage : average(selectedTotal, districtRows.size());
      return new SalesMetrics(selectedTotal, districtAverage, cityAverage, null);
    } catch (DataAccessException exception) {
      return SalesMetrics.empty();
    }
  }

  private Integer storeCount(MvpPublicDataMapping.Mapping mapping) {
    try {
      Integer count =
          jdbcTemplate.queryForObject(
              """
              SELECT COUNT(*)
              FROM small_business_stores
              WHERE (sigungu_name ILIKE ? OR administrative_dong_name ILIKE ? OR legal_dong_name ILIKE ?)
                AND (large_industry_name ILIKE ? OR medium_industry_name ILIKE ? OR small_industry_name ILIKE ?)
              """,
              Integer.class,
              like(mapping.districtKeyword()),
              like(mapping.districtKeyword()),
              like(mapping.districtKeyword()),
              like(mapping.industryKeyword()),
              like(mapping.industryKeyword()),
              like(mapping.industryKeyword()));
      return count != null && count > 0 ? count : null;
    } catch (DataAccessException exception) {
      return null;
    }
  }

  private KosisMetrics kosis(MvpPublicDataMapping.Mapping mapping) {
    try {
      List<KosisMetrics> rows =
          jdbcTemplate.query(
              """
              WITH latest_rates AS (
                SELECT * FROM kosis_business_rates
                WHERE reference_year = (SELECT MAX(reference_year) FROM kosis_business_rates)
              ), latest_survival AS (
                SELECT DISTINCT ON (industry_code) industry_code, industry_name, survival_rate
                FROM kosis_business_survival_rates
                WHERE item_name ILIKE '%1년%'
                ORDER BY industry_code, reference_year DESC
              )
              SELECT s.survival_rate, r.closed_business_count, r.active_business_count, r.new_business_count
              FROM latest_rates r
              LEFT JOIN latest_survival s ON s.industry_code = r.industry_code
              WHERE r.industry_name ILIKE ?
              LIMIT 1
              """,
              (rs, rowNum) ->
                  new KosisMetrics(
                      rs.getBigDecimal(1),
                      rs.getBigDecimal(2),
                      rs.getBigDecimal(3),
                      rs.getBigDecimal(4)),
              like(mapping.kosisIndustryKeyword()));
      return rows.isEmpty() ? KosisMetrics.empty() : rows.getFirst();
    } catch (DataAccessException exception) {
      return KosisMetrics.empty();
    }
  }

  private Integer traffic(MvpPublicDataMapping.Mapping mapping) {
    if (mapping.realtimePopulationArea() == null) return null;
    try {
      Long value =
          jdbcTemplate.queryForObject(
              """
              SELECT estimated_population
              FROM seoul_realtime_city_latest_populations
              WHERE area_name ILIKE ?
              ORDER BY population_time DESC
              LIMIT 1
              """,
              Long.class,
              like(mapping.realtimePopulationArea()));
      return value == null || value <= 0 ? null : Math.toIntExact(value);
    } catch (DataAccessException exception) {
      return null;
    }
  }

  private BigDecimal average(long total, int count) {
    return BigDecimal.valueOf(total).divide(BigDecimal.valueOf(count), 0, RoundingMode.HALF_UP);
  }

  private String like(String value) {
    return "%" + value + "%";
  }

  private record SalesRow(String areaName, long amount) {}

  private record SalesMetrics(
      Long totalAmount, BigDecimal districtAverage, BigDecimal cityAverage, BigDecimal growthRate) {
    static SalesMetrics empty() {
      return new SalesMetrics(null, null, null, null);
    }

    boolean available() {
      return totalAmount != null;
    }
  }

  private record KosisMetrics(
      BigDecimal survivalRate,
      BigDecimal closedBusinesses,
      BigDecimal activeBusinesses,
      BigDecimal newBusinesses) {
    static KosisMetrics empty() {
      return new KosisMetrics(null, null, null, null);
    }

    boolean available() {
      return survivalRate != null || activeBusinesses != null;
    }
  }
}
