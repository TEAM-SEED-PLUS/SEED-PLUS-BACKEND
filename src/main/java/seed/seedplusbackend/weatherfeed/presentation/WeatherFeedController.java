package seed.seedplusbackend.weatherfeed.presentation;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import seed.seedplusbackend.global.response.ApiResponse;
import seed.seedplusbackend.weatherfeed.application.WeatherFeedService;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/weather-feeds")
public class WeatherFeedController implements WeatherFeedApi {

  private final WeatherFeedService weatherFeedService;

  @Override
  public ResponseEntity<ApiResponse<JsonNode>> getOverview(
      @RequestParam(required = false) String date,
      @RequestParam(required = false) String time,
      @RequestParam(name = "time_band", required = false) String timeBand) {
    return ResponseEntity.ok(
        ApiResponse.success(weatherFeedService.getOverview(date, time, timeBand)));
  }

  @Override
  public ResponseEntity<ApiResponse<JsonNode>> getDetail(
      @RequestParam String district,
      @RequestParam(required = false) String date,
      @RequestParam(required = false) String time,
      @RequestParam(name = "time_band", required = false) String timeBand) {
    return ResponseEntity.ok(
        ApiResponse.success(weatherFeedService.getDetail(district, date, time, timeBand)));
  }
}
