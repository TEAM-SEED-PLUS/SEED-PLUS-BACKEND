package seed.seedplusbackend.weatherfeed.application;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import seed.seedplusbackend.weatherfeed.application.port.WeatherFeedClient;

@Service
@RequiredArgsConstructor
public class WeatherFeedService {

  private final WeatherFeedClient weatherFeedClient;

  public JsonNode getOverview(String date, String time, String timeBand) {
    return weatherFeedClient.getOverview(date, time, timeBand);
  }

  public JsonNode getDetail(String district, String date, String time, String timeBand) {
    return weatherFeedClient.getDetail(district, date, time, timeBand);
  }
}
