package seed.seedplusbackend.commercial.infrastructure.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

public record SeoulCommercialEstimatedSalesApiResponse(
    @JsonProperty("VwsmTrdarSelngQq") Body body, @JsonProperty("RESULT") Result result) {

  public record Body(
      @JsonProperty("list_total_count") Integer totalCount,
      @JsonProperty("RESULT") Result result,
      @JsonProperty("row") List<Row> rows) {}

  public record Result(
      @JsonProperty("CODE") String code, @JsonProperty("MESSAGE") String message) {}

  public record Row(
      @JsonProperty("STDR_YYQU_CD") String stdrYyquCd,
      @JsonProperty("TRDAR_SE_CD") String trdarSeCd,
      @JsonProperty("TRDAR_SE_CD_NM") String trdarSeCdNm,
      @JsonProperty("TRDAR_CD") String trdarCd,
      @JsonProperty("TRDAR_CD_NM") String trdarCdNm,
      @JsonProperty("SVC_INDUTY_CD") String svcIndutyCd,
      @JsonProperty("SVC_INDUTY_CD_NM") String svcIndutyCdNm,
      @JsonProperty("THSMON_SELNG_AMT") @SeoulBigDecimal BigDecimal thsmonSelngAmt,
      @JsonProperty("THSMON_SELNG_CO") @SeoulBigDecimal BigDecimal thsmonSelngCo,
      @JsonProperty("MDWK_SELNG_AMT") @SeoulBigDecimal BigDecimal mdwkSelngAmt,
      @JsonProperty("WKEND_SELNG_AMT") @SeoulBigDecimal BigDecimal wkendSelngAmt,
      @JsonProperty("MON_SELNG_AMT") @SeoulBigDecimal BigDecimal monSelngAmt,
      @JsonProperty("TUES_SELNG_AMT") @SeoulBigDecimal BigDecimal tuesSelngAmt,
      @JsonProperty("WED_SELNG_AMT") @SeoulBigDecimal BigDecimal wedSelngAmt,
      @JsonProperty("THUR_SELNG_AMT") @SeoulBigDecimal BigDecimal thurSelngAmt,
      @JsonProperty("FRI_SELNG_AMT") @SeoulBigDecimal BigDecimal friSelngAmt,
      @JsonProperty("SAT_SELNG_AMT") @SeoulBigDecimal BigDecimal satSelngAmt,
      @JsonProperty("SUN_SELNG_AMT") @SeoulBigDecimal BigDecimal sunSelngAmt,
      @JsonProperty("TMZON_00_06_SELNG_AMT") @SeoulBigDecimal BigDecimal tmzon0006SelngAmt,
      @JsonProperty("TMZON_06_11_SELNG_AMT") @SeoulBigDecimal BigDecimal tmzon0611SelngAmt,
      @JsonProperty("TMZON_11_14_SELNG_AMT") @SeoulBigDecimal BigDecimal tmzon1114SelngAmt,
      @JsonProperty("TMZON_14_17_SELNG_AMT") @SeoulBigDecimal BigDecimal tmzon1417SelngAmt,
      @JsonProperty("TMZON_17_21_SELNG_AMT") @SeoulBigDecimal BigDecimal tmzon1721SelngAmt,
      @JsonProperty("TMZON_21_24_SELNG_AMT") @SeoulBigDecimal BigDecimal tmzon2124SelngAmt,
      @JsonProperty("ML_SELNG_AMT") @SeoulBigDecimal BigDecimal mlSelngAmt,
      @JsonProperty("FML_SELNG_AMT") @SeoulBigDecimal BigDecimal fmlSelngAmt,
      @JsonProperty("AGRDE_10_SELNG_AMT") @SeoulBigDecimal BigDecimal agrde10SelngAmt,
      @JsonProperty("AGRDE_20_SELNG_AMT") @SeoulBigDecimal BigDecimal agrde20SelngAmt,
      @JsonProperty("AGRDE_30_SELNG_AMT") @SeoulBigDecimal BigDecimal agrde30SelngAmt,
      @JsonProperty("AGRDE_40_SELNG_AMT") @SeoulBigDecimal BigDecimal agrde40SelngAmt,
      @JsonProperty("AGRDE_50_SELNG_AMT") @SeoulBigDecimal BigDecimal agrde50SelngAmt,
      @JsonProperty("AGRDE_60_ABOVE_SELNG_AMT") @SeoulBigDecimal BigDecimal agrde60AboveSelngAmt,
      @JsonProperty("MDWK_SELNG_CO") @SeoulBigDecimal BigDecimal mdwkSelngCo,
      @JsonProperty("WKEND_SELNG_CO") @SeoulBigDecimal BigDecimal wkendSelngCo,
      @JsonProperty("MON_SELNG_CO") @SeoulBigDecimal BigDecimal monSelngCo,
      @JsonProperty("TUES_SELNG_CO") @SeoulBigDecimal BigDecimal tuesSelngCo,
      @JsonProperty("WED_SELNG_CO") @SeoulBigDecimal BigDecimal wedSelngCo,
      @JsonProperty("THUR_SELNG_CO") @SeoulBigDecimal BigDecimal thurSelngCo,
      @JsonProperty("FRI_SELNG_CO") @SeoulBigDecimal BigDecimal friSelngCo,
      @JsonProperty("SAT_SELNG_CO") @SeoulBigDecimal BigDecimal satSelngCo,
      @JsonProperty("SUN_SELNG_CO") @SeoulBigDecimal BigDecimal sunSelngCo,
      @JsonProperty("TMZON_00_06_SELNG_CO") @SeoulBigDecimal BigDecimal tmzon0006SelngCo,
      @JsonProperty("TMZON_06_11_SELNG_CO") @SeoulBigDecimal BigDecimal tmzon0611SelngCo,
      @JsonProperty("TMZON_11_14_SELNG_CO") @SeoulBigDecimal BigDecimal tmzon1114SelngCo,
      @JsonProperty("TMZON_14_17_SELNG_CO") @SeoulBigDecimal BigDecimal tmzon1417SelngCo,
      @JsonProperty("TMZON_17_21_SELNG_CO") @SeoulBigDecimal BigDecimal tmzon1721SelngCo,
      @JsonProperty("TMZON_21_24_SELNG_CO") @SeoulBigDecimal BigDecimal tmzon2124SelngCo,
      @JsonProperty("ML_SELNG_CO") @SeoulBigDecimal BigDecimal mlSelngCo,
      @JsonProperty("FML_SELNG_CO") @SeoulBigDecimal BigDecimal fmlSelngCo,
      @JsonProperty("AGRDE_10_SELNG_CO") @SeoulBigDecimal BigDecimal agrde10SelngCo,
      @JsonProperty("AGRDE_20_SELNG_CO") @SeoulBigDecimal BigDecimal agrde20SelngCo,
      @JsonProperty("AGRDE_30_SELNG_CO") @SeoulBigDecimal BigDecimal agrde30SelngCo,
      @JsonProperty("AGRDE_40_SELNG_CO") @SeoulBigDecimal BigDecimal agrde40SelngCo,
      @JsonProperty("AGRDE_50_SELNG_CO") @SeoulBigDecimal BigDecimal agrde50SelngCo,
      @JsonProperty("AGRDE_60_ABOVE_SELNG_CO") @SeoulBigDecimal BigDecimal agrde60AboveSelngCo) {}
}
