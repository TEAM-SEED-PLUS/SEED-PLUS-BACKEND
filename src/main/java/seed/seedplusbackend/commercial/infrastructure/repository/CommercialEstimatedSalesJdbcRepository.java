package seed.seedplusbackend.commercial.infrastructure.repository;

import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import seed.seedplusbackend.commercial.application.port.CommercialEstimatedSalesStorePort;
import seed.seedplusbackend.commercial.application.result.CommercialEstimatedSalesRowResult;

@Repository
@RequiredArgsConstructor
public class CommercialEstimatedSalesJdbcRepository implements CommercialEstimatedSalesStorePort {

  private final JdbcTemplate jdbcTemplate;

  @Override
  public void upsertAll(List<CommercialEstimatedSalesRowResult> rows) {
    if (rows == null || rows.isEmpty()) {
      return;
    }

    String sql =
        """
            INSERT INTO commercial_estimated_sales (
              stdr_yyqu_cd,
              trdar_se_cd,
              trdar_se_cd_nm,
              trdar_cd,
              trdar_cd_nm,
              svc_induty_cd,
              svc_induty_cd_nm,
              thsmon_selng_amt,
              thsmon_selng_co,
              mdwk_selng_amt,
              wkend_selng_amt,
              mon_selng_amt,
              tues_selng_amt,
              wed_selng_amt,
              thur_selng_amt,
              fri_selng_amt,
              sat_selng_amt,
              sun_selng_amt,
              tmzon_00_06_selng_amt,
              tmzon_06_11_selng_amt,
              tmzon_11_14_selng_amt,
              tmzon_14_17_selng_amt,
              tmzon_17_21_selng_amt,
              tmzon_21_24_selng_amt,
              ml_selng_amt,
              fml_selng_amt,
              agrde_10_selng_amt,
              agrde_20_selng_amt,
              agrde_30_selng_amt,
              agrde_40_selng_amt,
              agrde_50_selng_amt,
              agrde_60_above_selng_amt,
              mdwk_selng_co,
              wkend_selng_co,
              mon_selng_co,
              tues_selng_co,
              wed_selng_co,
              thur_selng_co,
              fri_selng_co,
              sat_selng_co,
              sun_selng_co,
              tmzon_00_06_selng_co,
              tmzon_06_11_selng_co,
              tmzon_11_14_selng_co,
              tmzon_14_17_selng_co,
              tmzon_17_21_selng_co,
              tmzon_21_24_selng_co,
              ml_selng_co,
              fml_selng_co,
              agrde_10_selng_co,
              agrde_20_selng_co,
              agrde_30_selng_co,
              agrde_40_selng_co,
              agrde_50_selng_co,
              agrde_60_above_selng_co,
              collected_at,
              updated_at
            ) VALUES (
              ?, ?, ?, ?, ?, ?, ?,
              ?, ?,
              ?, ?, ?, ?, ?, ?, ?, ?, ?,
              ?, ?, ?, ?, ?, ?,
              ?, ?,
              ?, ?, ?, ?, ?, ?,
              ?, ?, ?, ?, ?, ?, ?, ?, ?,
              ?, ?, ?, ?, ?, ?,
              ?, ?,
              ?, ?, ?, ?, ?, ?,
              now(),
              now()
            )
            ON CONFLICT (stdr_yyqu_cd, trdar_cd, svc_induty_cd)
            DO UPDATE SET
              trdar_se_cd = EXCLUDED.trdar_se_cd,
              trdar_se_cd_nm = EXCLUDED.trdar_se_cd_nm,
              trdar_cd_nm = EXCLUDED.trdar_cd_nm,
              svc_induty_cd_nm = EXCLUDED.svc_induty_cd_nm,
              thsmon_selng_amt = EXCLUDED.thsmon_selng_amt,
              thsmon_selng_co = EXCLUDED.thsmon_selng_co,
              mdwk_selng_amt = EXCLUDED.mdwk_selng_amt,
              wkend_selng_amt = EXCLUDED.wkend_selng_amt,
              mon_selng_amt = EXCLUDED.mon_selng_amt,
              tues_selng_amt = EXCLUDED.tues_selng_amt,
              wed_selng_amt = EXCLUDED.wed_selng_amt,
              thur_selng_amt = EXCLUDED.thur_selng_amt,
              fri_selng_amt = EXCLUDED.fri_selng_amt,
              sat_selng_amt = EXCLUDED.sat_selng_amt,
              sun_selng_amt = EXCLUDED.sun_selng_amt,
              tmzon_00_06_selng_amt = EXCLUDED.tmzon_00_06_selng_amt,
              tmzon_06_11_selng_amt = EXCLUDED.tmzon_06_11_selng_amt,
              tmzon_11_14_selng_amt = EXCLUDED.tmzon_11_14_selng_amt,
              tmzon_14_17_selng_amt = EXCLUDED.tmzon_14_17_selng_amt,
              tmzon_17_21_selng_amt = EXCLUDED.tmzon_17_21_selng_amt,
              tmzon_21_24_selng_amt = EXCLUDED.tmzon_21_24_selng_amt,
              ml_selng_amt = EXCLUDED.ml_selng_amt,
              fml_selng_amt = EXCLUDED.fml_selng_amt,
              agrde_10_selng_amt = EXCLUDED.agrde_10_selng_amt,
              agrde_20_selng_amt = EXCLUDED.agrde_20_selng_amt,
              agrde_30_selng_amt = EXCLUDED.agrde_30_selng_amt,
              agrde_40_selng_amt = EXCLUDED.agrde_40_selng_amt,
              agrde_50_selng_amt = EXCLUDED.agrde_50_selng_amt,
              agrde_60_above_selng_amt = EXCLUDED.agrde_60_above_selng_amt,
              mdwk_selng_co = EXCLUDED.mdwk_selng_co,
              wkend_selng_co = EXCLUDED.wkend_selng_co,
              mon_selng_co = EXCLUDED.mon_selng_co,
              tues_selng_co = EXCLUDED.tues_selng_co,
              wed_selng_co = EXCLUDED.wed_selng_co,
              thur_selng_co = EXCLUDED.thur_selng_co,
              fri_selng_co = EXCLUDED.fri_selng_co,
              sat_selng_co = EXCLUDED.sat_selng_co,
              sun_selng_co = EXCLUDED.sun_selng_co,
              tmzon_00_06_selng_co = EXCLUDED.tmzon_00_06_selng_co,
              tmzon_06_11_selng_co = EXCLUDED.tmzon_06_11_selng_co,
              tmzon_11_14_selng_co = EXCLUDED.tmzon_11_14_selng_co,
              tmzon_14_17_selng_co = EXCLUDED.tmzon_14_17_selng_co,
              tmzon_17_21_selng_co = EXCLUDED.tmzon_17_21_selng_co,
              tmzon_21_24_selng_co = EXCLUDED.tmzon_21_24_selng_co,
              ml_selng_co = EXCLUDED.ml_selng_co,
              fml_selng_co = EXCLUDED.fml_selng_co,
              agrde_10_selng_co = EXCLUDED.agrde_10_selng_co,
              agrde_20_selng_co = EXCLUDED.agrde_20_selng_co,
              agrde_30_selng_co = EXCLUDED.agrde_30_selng_co,
              agrde_40_selng_co = EXCLUDED.agrde_40_selng_co,
              agrde_50_selng_co = EXCLUDED.agrde_50_selng_co,
              agrde_60_above_selng_co = EXCLUDED.agrde_60_above_selng_co,
              collected_at = now(),
              updated_at = now()
            """;

    jdbcTemplate.batchUpdate(
        sql,
        rows,
        500,
        (ps, row) -> {
          int index = 1;

          ps.setString(index++, row.stdrYyquCd());
          ps.setString(index++, row.trdarSeCd());
          ps.setString(index++, row.trdarSeCdNm());
          ps.setString(index++, row.trdarCd());
          ps.setString(index++, row.trdarCdNm());
          ps.setString(index++, row.svcIndutyCd());
          ps.setString(index++, row.svcIndutyCdNm());

          ps.setLong(index++, toLong(row.thsmonSelngAmt()));
          ps.setLong(index++, toLong(row.thsmonSelngCo()));

          ps.setLong(index++, toLong(row.mdwkSelngAmt()));
          ps.setLong(index++, toLong(row.wkendSelngAmt()));
          ps.setLong(index++, toLong(row.monSelngAmt()));
          ps.setLong(index++, toLong(row.tuesSelngAmt()));
          ps.setLong(index++, toLong(row.wedSelngAmt()));
          ps.setLong(index++, toLong(row.thurSelngAmt()));
          ps.setLong(index++, toLong(row.friSelngAmt()));
          ps.setLong(index++, toLong(row.satSelngAmt()));
          ps.setLong(index++, toLong(row.sunSelngAmt()));

          ps.setLong(index++, toLong(row.tmzon0006SelngAmt()));
          ps.setLong(index++, toLong(row.tmzon0611SelngAmt()));
          ps.setLong(index++, toLong(row.tmzon1114SelngAmt()));
          ps.setLong(index++, toLong(row.tmzon1417SelngAmt()));
          ps.setLong(index++, toLong(row.tmzon1721SelngAmt()));
          ps.setLong(index++, toLong(row.tmzon2124SelngAmt()));

          ps.setLong(index++, toLong(row.mlSelngAmt()));
          ps.setLong(index++, toLong(row.fmlSelngAmt()));

          ps.setLong(index++, toLong(row.agrde10SelngAmt()));
          ps.setLong(index++, toLong(row.agrde20SelngAmt()));
          ps.setLong(index++, toLong(row.agrde30SelngAmt()));
          ps.setLong(index++, toLong(row.agrde40SelngAmt()));
          ps.setLong(index++, toLong(row.agrde50SelngAmt()));
          ps.setLong(index++, toLong(row.agrde60AboveSelngAmt()));

          ps.setLong(index++, toLong(row.mdwkSelngCo()));
          ps.setLong(index++, toLong(row.wkendSelngCo()));
          ps.setLong(index++, toLong(row.monSelngCo()));
          ps.setLong(index++, toLong(row.tuesSelngCo()));
          ps.setLong(index++, toLong(row.wedSelngCo()));
          ps.setLong(index++, toLong(row.thurSelngCo()));
          ps.setLong(index++, toLong(row.friSelngCo()));
          ps.setLong(index++, toLong(row.satSelngCo()));
          ps.setLong(index++, toLong(row.sunSelngCo()));

          ps.setLong(index++, toLong(row.tmzon0006SelngCo()));
          ps.setLong(index++, toLong(row.tmzon0611SelngCo()));
          ps.setLong(index++, toLong(row.tmzon1114SelngCo()));
          ps.setLong(index++, toLong(row.tmzon1417SelngCo()));
          ps.setLong(index++, toLong(row.tmzon1721SelngCo()));
          ps.setLong(index++, toLong(row.tmzon2124SelngCo()));

          ps.setLong(index++, toLong(row.mlSelngCo()));
          ps.setLong(index++, toLong(row.fmlSelngCo()));

          ps.setLong(index++, toLong(row.agrde10SelngCo()));
          ps.setLong(index++, toLong(row.agrde20SelngCo()));
          ps.setLong(index++, toLong(row.agrde30SelngCo()));
          ps.setLong(index++, toLong(row.agrde40SelngCo()));
          ps.setLong(index++, toLong(row.agrde50SelngCo()));
          ps.setLong(index++, toLong(row.agrde60AboveSelngCo()));
        });
  }

  private long toLong(BigDecimal value) {
    if (value == null) {
      return 0L;
    }

    return value.longValue();
  }
}
