package seed.seedplusbackend.commercial.infrastructure.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SeoulSdotFootTrafficApiResponse(
    @JsonProperty("IotVdata018") Body body, @JsonProperty("RESULT") Result result) {

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Body(
      @JsonProperty("list_total_count") Integer totalCount,
      @JsonProperty("RESULT") Result result,
      @JsonProperty("row") List<Row> rows) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Result(
      @JsonProperty("CODE") String code, @JsonProperty("MESSAGE") String message) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Row(
      @JsonProperty("MODEL_NM") String modelName,
      @JsonProperty("SERIAL_NO") String serialNumber,
      @JsonProperty("SENSING_TIME") String sensingTime,
      @JsonProperty("REGION") String regionType,
      @JsonProperty("AUTONOMOUS_DISTRICT") String autonomousDistrict,
      @JsonProperty("ADMINISTRATIVE_DISTRICT") String administrativeDistrict,
      @JsonProperty("VISITOR_COUNT") String visitorCount,
      @JsonProperty("REG_DTTM") String registeredAt) {}
}
