package seed.seedplusbackend.commercial.infrastructure.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import seed.seedplusbackend.commercial.application.port.SeoulSdotFootTrafficClientPort;
import seed.seedplusbackend.commercial.application.result.SeoulSdotFootTrafficPageResult;
import seed.seedplusbackend.commercial.application.result.SeoulSdotFootTrafficRowResult;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;

@Slf4j
@Component
public class SeoulSdotFootTrafficClient implements SeoulSdotFootTrafficClientPort {

  private static final String SUCCESS_CODE = "INFO-000";
  private static final String NO_DATA_CODE = "INFO-200";
  private static final DateTimeFormatter SENSING_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss");
  private static final DateTimeFormatter REGISTERED_AT_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private final RestClient restClient;
  private final ObjectMapper objectMapper;
  private final SeoulSdotOpenApiProperties properties;

  public SeoulSdotFootTrafficClient(
      @Qualifier("externalRestClientBuilder") RestClient.Builder restClientBuilder,
      ObjectMapper objectMapper,
      SeoulSdotOpenApiProperties properties) {
    this.restClient = restClientBuilder.clone().baseUrl(properties.baseUrl()).build();
    this.objectMapper = objectMapper;
    this.properties = properties;
  }

  @Override
  public SeoulSdotFootTrafficPageResult fetch(int startIndex, int endIndex) {
    byte[] responseBytes;
    try {
      responseBytes =
          restClient
              .get()
              .uri(
                  uriBuilder ->
                      uriBuilder
                          .pathSegment(
                              properties.key(),
                              properties.type(),
                              properties.serviceName(),
                              String.valueOf(startIndex),
                              String.valueOf(endIndex))
                          .build())
              .retrieve()
              .onStatus(
                  HttpStatusCode::isError,
                  (request, response) -> {
                    throw new ApplicationException(ErrorCode.SEOUL_OPEN_API_REQUEST_FAILED);
                  })
              .body(byte[].class);
    } catch (ApplicationException exception) {
      throw exception;
    } catch (RestClientException exception) {
      throw new ApplicationException(ErrorCode.SEOUL_OPEN_API_REQUEST_FAILED, exception);
    }

    if (responseBytes == null || responseBytes.length == 0) {
      throw new ApplicationException(ErrorCode.SEOUL_OPEN_API_INVALID_RESPONSE);
    }

    SeoulSdotFootTrafficApiResponse response;
    try {
      response =
          objectMapper.readValue(
              new String(responseBytes, StandardCharsets.UTF_8),
              SeoulSdotFootTrafficApiResponse.class);
    } catch (JsonProcessingException exception) {
      throw new ApplicationException(ErrorCode.SEOUL_OPEN_API_INVALID_RESPONSE, exception);
    }

    SeoulSdotFootTrafficApiResponse.Body body = validateAndGetBody(response);
    if (NO_DATA_CODE.equals(body.result().code())) {
      return new SeoulSdotFootTrafficPageResult(0, Collections.emptyList());
    }
    if (body.totalCount() == null || body.totalCount() < 0) {
      throw new ApplicationException(ErrorCode.SEOUL_OPEN_API_INVALID_RESPONSE);
    }

    List<SeoulSdotFootTrafficRowResult> rows = toValidResults(body.rows());
    if (body.totalCount() > 0 && body.rows() != null && !body.rows().isEmpty() && rows.isEmpty()) {
      throw new ApplicationException(ErrorCode.SEOUL_OPEN_API_INVALID_RESPONSE);
    }
    return new SeoulSdotFootTrafficPageResult(body.totalCount(), rows);
  }

  private SeoulSdotFootTrafficApiResponse.Body validateAndGetBody(
      SeoulSdotFootTrafficApiResponse response) {
    if (response == null) {
      throw new ApplicationException(ErrorCode.SEOUL_OPEN_API_INVALID_RESPONSE);
    }
    if (response.body() == null) {
      if (response.result() != null) {
        throw new ApplicationException(ErrorCode.SEOUL_OPEN_API_REQUEST_FAILED);
      }
      throw new ApplicationException(ErrorCode.SEOUL_OPEN_API_INVALID_RESPONSE);
    }
    if (response.body().result() == null) {
      throw new ApplicationException(ErrorCode.SEOUL_OPEN_API_INVALID_RESPONSE);
    }
    String code = response.body().result().code();
    if (SUCCESS_CODE.equals(code) || NO_DATA_CODE.equals(code)) {
      return response.body();
    }
    throw new ApplicationException(ErrorCode.SEOUL_OPEN_API_REQUEST_FAILED);
  }

  private List<SeoulSdotFootTrafficRowResult> toValidResults(
      List<SeoulSdotFootTrafficApiResponse.Row> rows) {
    if (rows == null || rows.isEmpty()) {
      return Collections.emptyList();
    }
    List<SeoulSdotFootTrafficRowResult> results = new ArrayList<>();
    int skippedCount = 0;
    for (SeoulSdotFootTrafficApiResponse.Row row : rows) {
      try {
        results.add(toResult(row));
      } catch (IllegalArgumentException exception) {
        skippedCount++;
      }
    }
    if (skippedCount > 0) {
      log.warn("서울시 S-DoT 유동인구 응답에서 유효하지 않은 행을 제외했습니다. skippedCount={}", skippedCount);
    }
    return List.copyOf(results);
  }

  private SeoulSdotFootTrafficRowResult toResult(SeoulSdotFootTrafficApiResponse.Row row) {
    if (row == null
        || isBlank(row.serialNumber())
        || isBlank(row.sensingTime())
        || isBlank(row.visitorCount())) {
      throw new IllegalArgumentException("서울시 S-DoT 필수 응답값이 누락되었습니다.");
    }
    try {
      long visitorCount = Long.parseLong(row.visitorCount().replace(",", "").trim());
      if (visitorCount < 0) {
        throw new IllegalArgumentException("유동인구 측정값은 음수일 수 없습니다.");
      }
      return new SeoulSdotFootTrafficRowResult(
          row.modelName(),
          row.serialNumber(),
          LocalDateTime.parse(row.sensingTime().trim(), SENSING_TIME_FORMATTER),
          row.regionType(),
          row.autonomousDistrict(),
          row.administrativeDistrict(),
          visitorCount,
          parseRegisteredAt(row.registeredAt()));
    } catch (NumberFormatException | DateTimeParseException exception) {
      throw new IllegalArgumentException("서울시 S-DoT 응답 형식이 올바르지 않습니다.", exception);
    }
  }

  private LocalDateTime parseRegisteredAt(String value) {
    return isBlank(value) ? null : LocalDateTime.parse(value.trim(), REGISTERED_AT_FORMATTER);
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
