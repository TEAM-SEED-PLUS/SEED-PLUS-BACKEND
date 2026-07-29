package seed.seedplusbackend.commercial.infrastructure.client;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import seed.seedplusbackend.commercial.application.command.KosisBusinessSurvivalCollectCommand;
import seed.seedplusbackend.commercial.application.port.KosisBusinessSurvivalClientPort;
import seed.seedplusbackend.commercial.application.result.KosisBusinessSurvivalRowResult;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;

@Component
public class KosisBusinessSurvivalClient implements KosisBusinessSurvivalClientPort {

  private static final String OUTPUT_FIELDS =
      "ORG_ID,TBL_ID,TBL_NM,C1,C1_NM,C1_OBJ_NM,ITM_ID,ITM_NM,UNIT_NM,"
          + "PRD_SE,PRD_DE,DT,LST_CHN_DE";

  private final RestClient restClient;
  private final KosisBusinessSurvivalOpenApiProperties properties;

  public KosisBusinessSurvivalClient(
      @Qualifier("externalRestClientBuilder") RestClient.Builder restClientBuilder,
      KosisBusinessSurvivalOpenApiProperties properties) {
    this.restClient = restClientBuilder.clone().baseUrl(properties.baseUrl()).build();
    this.properties = properties;
  }

  @Override
  public List<KosisBusinessSurvivalRowResult> fetch(KosisBusinessSurvivalCollectCommand command) {
    List<KosisBusinessSurvivalApiResponse> response;
    try {
      response =
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
                        .queryParam("prdInterval", "1")
                        .queryParam("outputFields", OUTPUT_FIELDS);

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
                    throw new ApplicationException(ErrorCode.KOSIS_OPEN_API_REQUEST_FAILED);
                  })
              .body(new ParameterizedTypeReference<>() {});
    } catch (ApplicationException exception) {
      throw exception;
    } catch (RestClientException exception) {
      throw new ApplicationException(ErrorCode.KOSIS_OPEN_API_REQUEST_FAILED, exception);
    }

    if (response == null) {
      throw new ApplicationException(ErrorCode.KOSIS_OPEN_API_INVALID_RESPONSE);
    }

    try {
      return response.stream().map(this::toResult).toList();
    } catch (RuntimeException exception) {
      throw new ApplicationException(ErrorCode.KOSIS_OPEN_API_INVALID_RESPONSE, exception);
    }
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
}
