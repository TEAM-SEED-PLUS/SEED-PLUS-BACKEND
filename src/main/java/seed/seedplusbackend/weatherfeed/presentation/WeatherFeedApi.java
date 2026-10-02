package seed.seedplusbackend.weatherfeed.presentation;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.global.response.ApiResponse;
import seed.seedplusbackend.global.swagger.annotation.ApiErrorCodeExamples;

@Tag(name = "상권 날씨", description = "FastAPI 상권 날씨 피드 프록시 API")
public interface WeatherFeedApi {

  String DATE_PATTERN = "^\\d{4}-\\d{2}-\\d{2}$";
  String TIME_PATTERN = "^([01]\\d|2[0-3]):[0-5]\\d$";
  String TIME_BAND_PATTERN = "^(심야|아침|점심|오후|저녁)$";

  @Operation(summary = "자치구별 상권 날씨 요약 조회", operationId = "getWeatherFeedOverview")
  @ApiErrorCodeExamples({ErrorCode.INVALID_PARAMETER, ErrorCode.WEATHER_FEED_API_REQUEST_FAILED})
  @GetMapping("/overview")
  ResponseEntity<ApiResponse<JsonNode>> getOverview(
      @Parameter(description = "조회 날짜 (YYYY-MM-DD)") @RequestParam(required = false) String date,
      @Parameter(description = "조회 시간 (HH:mm)") @RequestParam(required = false) String time,
      @Parameter(description = "시간대 (심야, 아침, 점심, 오후, 저녁)")
          @RequestParam(name = "time_band", required = false)
          String timeBand);

  @Operation(summary = "자치구 상권 날씨 상세 조회", operationId = "getWeatherFeedDetail")
  @ApiErrorCodeExamples({ErrorCode.INVALID_PARAMETER, ErrorCode.WEATHER_FEED_API_REQUEST_FAILED})
  @GetMapping
  ResponseEntity<ApiResponse<JsonNode>> getDetail(
      @Parameter(description = "자치구명", example = "강남구", required = true) @RequestParam
          String district,
      @Parameter(description = "조회 날짜 (YYYY-MM-DD)") @RequestParam(required = false) String date,
      @Parameter(description = "조회 시간 (HH:mm)") @RequestParam(required = false) String time,
      @Parameter(description = "시간대 (심야, 아침, 점심, 오후, 저녁)")
          @RequestParam(name = "time_band", required = false)
          String timeBand);
}
