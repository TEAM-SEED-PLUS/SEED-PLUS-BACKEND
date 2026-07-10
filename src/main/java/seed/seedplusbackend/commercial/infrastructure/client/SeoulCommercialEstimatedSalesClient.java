package seed.seedplusbackend.commercial.infrastructure.client;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import seed.seedplusbackend.commercial.application.port.SeoulCommercialEstimatedSalesClientPort;
import seed.seedplusbackend.commercial.application.result.CommercialEstimatedSalesPageResult;
import seed.seedplusbackend.commercial.application.result.CommercialEstimatedSalesRowResult;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;

@Component
@RequiredArgsConstructor
public class SeoulCommercialEstimatedSalesClient
    implements SeoulCommercialEstimatedSalesClientPort {

  private static final String SUCCESS_CODE = "INFO-000";
  private static final String NO_DATA_CODE = "INFO-200";
  private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
  private static final Duration READ_TIMEOUT = Duration.ofSeconds(30);

  private final SeoulCommercialOpenApiProperties properties;

  @Override
  public CommercialEstimatedSalesPageResult fetchByQuarter(
      String stdrYyquCd, int startIndex, int endIndex) {
    SeoulCommercialEstimatedSalesApiResponse response =
            RestClient.builder()
                    .baseUrl(properties.baseUrl())
                    .requestFactory(createRequestFactory())
                    .build()
                    .get()
                    .uri(
                            uriBuilder ->
                                    uriBuilder
                                            .pathSegment(
                                                    properties.key(),
                                                    properties.type(),
                                                    properties.serviceName(),
                                                    String.valueOf(startIndex),
                                                    String.valueOf(endIndex),
                                                    stdrYyquCd)
                                            .build())
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::isError,
                            (request, clientResponse) -> {
                              throw new ApplicationException(ErrorCode.SEOUL_OPEN_API_REQUEST_FAILED);
                            })
                    .body(SeoulCommercialEstimatedSalesApiResponse.class);

    SeoulCommercialEstimatedSalesApiResponse.Body body = validateAndGetBody(response);

    if (isNoData(body)) {
      return new CommercialEstimatedSalesPageResult(0, Collections.emptyList());
    }

    List<CommercialEstimatedSalesRowResult> rows =
        body.rows() == null
            ? Collections.emptyList()
            : body.rows().stream().map(this::toResult).toList();

    int totalCount = body.totalCount() == null ? 0 : body.totalCount();

    return new CommercialEstimatedSalesPageResult(totalCount, rows);
  }

  private SeoulCommercialEstimatedSalesApiResponse.Body validateAndGetBody(
      SeoulCommercialEstimatedSalesApiResponse response) {
    if (response == null || response.body() == null) {
      throw new ApplicationException(ErrorCode.SEOUL_OPEN_API_INVALID_RESPONSE);
    }

    SeoulCommercialEstimatedSalesApiResponse.Body body = response.body();
    SeoulCommercialEstimatedSalesApiResponse.Result result = body.result();

    if (result == null) {
      throw new ApplicationException(ErrorCode.SEOUL_OPEN_API_INVALID_RESPONSE);
    }

    String code = result.code();

    if (SUCCESS_CODE.equals(code) || NO_DATA_CODE.equals(code)) {
      return body;
    }

    throw new ApplicationException(ErrorCode.SEOUL_OPEN_API_REQUEST_FAILED);
  }

  private boolean isNoData(SeoulCommercialEstimatedSalesApiResponse.Body body) {
    return NO_DATA_CODE.equals(body.result().code());
  }

  private SimpleClientHttpRequestFactory createRequestFactory() {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(CONNECT_TIMEOUT);
    factory.setReadTimeout(READ_TIMEOUT);
    return factory;
  }

  private CommercialEstimatedSalesRowResult toResult(
      SeoulCommercialEstimatedSalesApiResponse.Row row) {
    return new CommercialEstimatedSalesRowResult(
        row.stdrYyquCd(),
        row.trdarSeCd(),
        row.trdarSeCdNm(),
        row.trdarCd(),
        row.trdarCdNm(),
        row.svcIndutyCd(),
        row.svcIndutyCdNm(),
        row.thsmonSelngAmt(),
        row.thsmonSelngCo(),
        row.mdwkSelngAmt(),
        row.wkendSelngAmt(),
        row.monSelngAmt(),
        row.tuesSelngAmt(),
        row.wedSelngAmt(),
        row.thurSelngAmt(),
        row.friSelngAmt(),
        row.satSelngAmt(),
        row.sunSelngAmt(),
        row.tmzon0006SelngAmt(),
        row.tmzon0611SelngAmt(),
        row.tmzon1114SelngAmt(),
        row.tmzon1417SelngAmt(),
        row.tmzon1721SelngAmt(),
        row.tmzon2124SelngAmt(),
        row.mlSelngAmt(),
        row.fmlSelngAmt(),
        row.agrde10SelngAmt(),
        row.agrde20SelngAmt(),
        row.agrde30SelngAmt(),
        row.agrde40SelngAmt(),
        row.agrde50SelngAmt(),
        row.agrde60AboveSelngAmt(),
        row.mdwkSelngCo(),
        row.wkendSelngCo(),
        row.monSelngCo(),
        row.tuesSelngCo(),
        row.wedSelngCo(),
        row.thurSelngCo(),
        row.friSelngCo(),
        row.satSelngCo(),
        row.sunSelngCo(),
        row.tmzon0006SelngCo(),
        row.tmzon0611SelngCo(),
        row.tmzon1114SelngCo(),
        row.tmzon1417SelngCo(),
        row.tmzon1721SelngCo(),
        row.tmzon2124SelngCo(),
        row.mlSelngCo(),
        row.fmlSelngCo(),
        row.agrde10SelngCo(),
        row.agrde20SelngCo(),
        row.agrde30SelngCo(),
        row.agrde40SelngCo(),
        row.agrde50SelngCo(),
        row.agrde60AboveSelngCo());
  }
}
