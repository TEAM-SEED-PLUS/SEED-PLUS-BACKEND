package seed.seedplusbackend.commercial.infrastructure.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import seed.seedplusbackend.commercial.application.command.KosisBusinessSurvivalCollectCommand;
import seed.seedplusbackend.commercial.application.exception.KosisBusinessSurvivalApiRequestException;
import seed.seedplusbackend.commercial.application.port.KosisBusinessSurvivalClientPort;
import seed.seedplusbackend.commercial.application.result.KosisBusinessSurvivalRowResult;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;

@Slf4j
@Component
public class KosisBusinessSurvivalClient implements KosisBusinessSurvivalClientPort {

  private static final Set<String> SURVIVAL_RATE_ITEM_IDS =
      Set.of("T01", "T02", "T03", "T04", "T05", "T06", "T07");

  private final RestClient restClient;
  private final ObjectMapper objectMapper;
  private final KosisBusinessSurvivalOpenApiProperties properties;

  public KosisBusinessSurvivalClient(
      @Qualifier("externalRestClientBuilder") RestClient.Builder restClientBuilder,
      ObjectMapper objectMapper,
      KosisBusinessSurvivalOpenApiProperties properties) {
    this.restClient = restClientBuilder.clone().baseUrl(properties.baseUrl()).build();
    this.objectMapper = objectMapper;
    this.properties = properties;
  }

  @Override
  public List<KosisBusinessSurvivalRowResult> fetch(KosisBusinessSurvivalCollectCommand command) {
    byte[] responseBytes;
    try {
      responseBytes =
          restClient
              .get()
              .uri(
                  uriBuilder -> {
                    uriBuilder
                        .path(properties.endpoint())
                        .queryParam("method", "getList")
                        .queryParam("apiKey", properties.key())
                        .queryParam("format", "json")
                        .queryParam("jsonVD", "Y")
                        .queryParam("orgId", properties.organizationId())
                        .queryParam("tblId", properties.tableId())
                        .queryParam("objL1", "ALL")
                        .queryParam("itmId", "ALL")
                        .queryParam("prdSe", "Y")
                        .queryParam("prdInterval", "1");

                    if (command.latestYearCount() != null) {
                      uriBuilder.queryParam("newEstPrdCnt", command.latestYearCount());
                    } else {
                      uriBuilder
                          .queryParam("startPrdDe", command.startYear())
                          .queryParam("endPrdDe", command.endYear());
                    }
                    return uriBuilder.build();
                  })
              .retrieve()
              .onStatus(
                  HttpStatusCode::isError,
                  (request, clientResponse) -> {
                    throw requestException(clientResponse.getStatusCode());
                  })
              .body(byte[].class);
    } catch (ApplicationException exception) {
      throw exception;
    } catch (RestClientException exception) {
      throw new KosisBusinessSurvivalApiRequestException(true, exception);
    }

    if (responseBytes == null || responseBytes.length == 0) {
      throw new ApplicationException(ErrorCode.KOSIS_OPEN_API_INVALID_RESPONSE);
    }

    String responseBody = new String(responseBytes, StandardCharsets.UTF_8);
    if (responseBody.isBlank()) {
      throw new ApplicationException(ErrorCode.KOSIS_OPEN_API_INVALID_RESPONSE);
    }

    try {
      JsonNode root = objectMapper.readTree(responseBody);
      if (root != null && root.isObject() && root.has("err")) {
        log.warn(
            "KOSIS OpenAPI 오류 응답입니다. err={} errMsg={}",
            logValue(root.path("err").asText()),
            logValue(root.path("errMsg").asText()));
        throw new ApplicationException(ErrorCode.KOSIS_OPEN_API_INVALID_RESPONSE);
      }

      List<KosisBusinessSurvivalApiResponse> response =
          objectMapper.readValue(responseBody, new TypeReference<>() {});
      return toValidResults(response);
    } catch (JsonProcessingException exception) {
      log.warn(
          "KOSIS OpenAPI 응답 파싱에 실패했습니다. responsePreview={}", logValue(responseBody), exception);
      throw new ApplicationException(ErrorCode.KOSIS_OPEN_API_INVALID_RESPONSE, exception);
    }
  }

  private List<KosisBusinessSurvivalRowResult> toValidResults(
      List<KosisBusinessSurvivalApiResponse> response) {
    List<KosisBusinessSurvivalRowResult> results = new ArrayList<>();
    int skippedCount = 0;

    for (KosisBusinessSurvivalApiResponse row : response) {
      if (row == null) {
        skippedCount++;
        continue;
      }
      if (!SURVIVAL_RATE_ITEM_IDS.contains(row.itemId())) {
        continue;
      }

      try {
        results.add(toResult(row));
      } catch (IllegalArgumentException exception) {
        skippedCount++;
      }
    }

    if (skippedCount > 0) {
      log.warn("KOSIS 신생기업 생존율 응답에서 유효하지 않은 행을 제외했습니다. skippedCount={}", skippedCount);
    }
    return List.copyOf(results);
  }

  private KosisBusinessSurvivalRowResult toResult(KosisBusinessSurvivalApiResponse row) {
    if (isBlank(row.organizationId())
        || isBlank(row.tableId())
        || isBlank(row.industryCode())
        || isBlank(row.industryName())
        || isBlank(row.itemId())
        || isBlank(row.itemName())
        || isBlank(row.unitName())
        || isBlank(row.periodType())
        || isBlank(row.referenceYear())
        || isBlank(row.value())) {
      throw new IllegalArgumentException("KOSIS 필수 응답값이 누락되었습니다.");
    }

    return new KosisBusinessSurvivalRowResult(
        row.organizationId(),
        row.tableId(),
        row.tableName(),
        row.industryCode(),
        row.industryName(),
        row.classificationName(),
        row.itemId(),
        row.itemName(),
        row.unitName(),
        row.periodType(),
        Integer.parseInt(row.referenceYear()),
        new BigDecimal(row.value().replace(",", "")),
        row.sourceUpdatedAt());
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private String logValue(String value) {
    String singleLine = value.replaceAll("[\\r\\n\\t]+", " ");
    return singleLine.length() <= 500 ? singleLine : singleLine.substring(0, 500) + "...";
  }

  static KosisBusinessSurvivalApiRequestException requestException(HttpStatusCode statusCode) {
    return new KosisBusinessSurvivalApiRequestException(statusCode.is5xxServerError());
  }
}
