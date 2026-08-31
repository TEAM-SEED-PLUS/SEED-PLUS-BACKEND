package seed.seedplusbackend.commercial.infrastructure.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KosisBusinessSurvivalApiResponse(
    @JsonProperty("ORG_ID") String organizationId,
    @JsonProperty("TBL_ID") String tableId,
    @JsonProperty("TBL_NM") String tableName,
    @JsonProperty("C1") String industryCode,
    @JsonProperty("C1_NM") String industryName,
    @JsonProperty("C1_OBJ_NM") String classificationName,
    @JsonProperty("ITM_ID") String itemId,
    @JsonProperty("ITM_NM") String itemName,
    @JsonProperty("UNIT_NM") String unitName,
    @JsonProperty("PRD_SE") String periodType,
    @JsonProperty("PRD_DE") String referenceYear,
    @JsonProperty("DT") String value,
    @JsonProperty("LST_CHN_DE") String sourceUpdatedAt) {}
