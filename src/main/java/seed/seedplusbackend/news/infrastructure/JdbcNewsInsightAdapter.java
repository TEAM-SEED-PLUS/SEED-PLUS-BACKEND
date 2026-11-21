package seed.seedplusbackend.news.infrastructure;

import java.sql.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import seed.seedplusbackend.news.application.port.NewsInsightPort;
import seed.seedplusbackend.news.domain.*;

@Repository
@RequiredArgsConstructor
public class JdbcNewsInsightAdapter implements NewsInsightPort {
  private final JdbcTemplate jdbc;
  private static final String SALES =
      """
   WITH ranked AS (
     SELECT s.*,concat(trdar_cd,':',svc_induty_cd,':',stdr_yyqu_cd) AS item_key,
       row_number() OVER(PARTITION BY trdar_cd,svc_induty_cd ORDER BY stdr_yyqu_cd DESC) AS recency
     FROM commercial_estimated_sales s WHERE stdr_yyqu_cd ~ '^[0-9]{4}[1-4]$'
   )
   SELECT s.item_key,concat(s.trdar_cd_nm,' ',s.svc_induty_cd_nm) AS target,
     left(s.stdr_yyqu_cd,4)::int AS year,right(s.stdr_yyqu_cd,1)::int AS quarter,
     s.thsmon_selng_amt AS current_value,p.thsmon_selng_amt AS previous_value,
     ARRAY(SELECT DISTINCT r.region_id FROM region_external_code_mappings m JOIN regions r
       ON r.code=left(m.region_code,5) AND r.code_type='SIGUNGU'
       WHERE m.source='SEOUL_ESTIMATED_SALES' AND m.external_code=s.trdar_cd) AS regions,
     ARRAY(SELECT DISTINCT m.industry_id FROM industry_external_code_mappings m
       WHERE m.source='SEOUL_ESTIMATED_SALES' AND m.external_code=s.svc_induty_cd) AS industries
   FROM ranked s LEFT JOIN commercial_estimated_sales p
     ON p.trdar_cd=s.trdar_cd AND p.svc_induty_cd=s.svc_induty_cd
     AND p.stdr_yyqu_cd=CASE WHEN right(s.stdr_yyqu_cd,1)='1'
       THEN ((left(s.stdr_yyqu_cd,4)::int-1)::text || '4')
       ELSE left(s.stdr_yyqu_cd,4) || ((right(s.stdr_yyqu_cd,1)::int-1)::text) END
   WHERE s.item_key>? AND (s.recency=1 OR EXISTS(
     SELECT 1 FROM news_articles n WHERE n.source='SEOUL_SALES' AND n.source_key=s.item_key))
   ORDER BY s.item_key LIMIT ?
   """;
  private static final String RENT =
      """
   WITH ranked AS (
     SELECT m.*,a.source_area_key,a.area_path,a.region_id,a.commercial_area_id,
       concat(a.source_area_key,':',m.reference_year,m.reference_quarter) AS item_key,
       row_number() OVER(PARTITION BY m.reb_small_retail_rent_area_id
         ORDER BY m.reference_year DESC,m.reference_quarter DESC) AS recency
     FROM reb_small_retail_rent_metrics m JOIN reb_small_retail_rent_areas a USING(reb_small_retail_rent_area_id)
   )
   SELECT s.item_key,s.area_path AS target,s.reference_year AS year,s.reference_quarter AS quarter,
     s.rent_per_square_meter_thousand_krw AS current_value,
     p.rent_per_square_meter_thousand_krw AS previous_value,
     ARRAY(SELECT DISTINCT district.region_id FROM regions r JOIN regions district
       ON district.sido=r.sido AND district.sigungu=r.sigungu AND district.code_type='SIGUNGU'
       WHERE r.region_id=s.region_id OR r.region_id IN(
         SELECT region_id FROM commercial_area_region_mappings WHERE commercial_area_id=s.commercial_area_id)) AS regions,
     ARRAY[]::bigint[] AS industries
   FROM ranked s LEFT JOIN reb_small_retail_rent_metrics p
     ON p.reb_small_retail_rent_area_id=s.reb_small_retail_rent_area_id
     AND p.reference_year=CASE WHEN s.reference_quarter=1 THEN s.reference_year-1 ELSE s.reference_year END
     AND p.reference_quarter=CASE WHEN s.reference_quarter=1 THEN 4 ELSE s.reference_quarter-1 END
   WHERE s.item_key>? AND (s.recency=1 OR EXISTS(
     SELECT 1 FROM news_articles n WHERE n.source='REB_RENT' AND n.source_key=s.item_key))
   ORDER BY s.item_key LIMIT ?
   """;

  @Override
  public List<InsightComparison> candidates(NewsSource source, String after, int limit) {
    String sql =
        switch (source) {
          case SEOUL_SALES -> SALES;
          case REB_RENT -> RENT;
          default -> throw new IllegalArgumentException("Not an insight source");
        };
    return jdbc.query(
        sql,
        (rs, n) ->
            new InsightComparison(
                source,
                rs.getString("item_key"),
                rs.getString("target"),
                rs.getInt("year"),
                rs.getInt("quarter"),
                rs.getBigDecimal("current_value"),
                rs.getBigDecimal("previous_value"),
                ids(rs.getArray("regions")),
                ids(rs.getArray("industries"))),
        after,
        limit);
  }

  private static List<Long> ids(java.sql.Array array) throws SQLException {
    if (array == null) return List.of();
    try {
      return Arrays.stream((Object[]) array.getArray()).map(x -> ((Number) x).longValue()).toList();
    } finally {
      array.free();
    }
  }
}
