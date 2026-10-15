package seed.seedplusbackend.weatherfeed.application.port;

import com.fasterxml.jackson.databind.JsonNode;

public interface WeatherFeedClient {

  JsonNode getOverview(String date, String time, String timeBand);

  JsonNode getDetail(String district, String date, String time, String timeBand);
}
