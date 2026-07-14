package seed.seedplusbackend.commercial.infrastructure.client;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;
import seed.seedplusbackend.commercial.application.command.SmallBusinessStoreCollectCommand;
import seed.seedplusbackend.commercial.application.port.SmallBusinessStoreClientPort;
import seed.seedplusbackend.commercial.application.result.SmallBusinessStorePageResult;
import seed.seedplusbackend.commercial.application.result.SmallBusinessStoreRowResult;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;

@Component
@Slf4j
@RequiredArgsConstructor
public class SmallBusinessStoreClient implements SmallBusinessStoreClientPort {

  private static final String SUCCESS_CODE = "00";
  private static final String NO_DATA_CODE = "03";
  private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
  private static final Duration READ_TIMEOUT = Duration.ofSeconds(30);

  private final SmallBusinessStoreOpenApiProperties properties;

  @Override
  public SmallBusinessStorePageResult fetch(
      SmallBusinessStoreCollectCommand command, int pageNumber, int numberOfRows) {
    SmallBusinessStoreApiEnvelope envelope =
        RestClient.builder()
            .baseUrl(properties.baseUrl())
            .requestFactory(requestFactory())
            .build()
            .get()
            .uri(uriBuilder -> buildUri(uriBuilder, command, pageNumber, numberOfRows).build())
            .retrieve()
            .onStatus(
                HttpStatusCode::isError,
                (request, clientResponse) -> {
                  throw new ApplicationException(ErrorCode.SMALL_BUSINESS_STORE_API_REQUEST_FAILED);
                })
            .body(SmallBusinessStoreApiEnvelope.class);

    SmallBusinessStoreApiResponse response = envelope == null ? null : envelope.unwrap();
    if (isNoData(response)) {
      return new SmallBusinessStorePageResult(0, Collections.emptyList());
    }
    validate(response);
    SmallBusinessStoreApiResponse.Body body = response.body();
    List<SmallBusinessStoreRowResult> rows =
        body.items() == null
            ? Collections.emptyList()
            : body.items().stream().map(this::toResult).toList();
    return new SmallBusinessStorePageResult(body.totalCount(), rows);
  }

  private UriBuilder buildUri(
      UriBuilder builder,
      SmallBusinessStoreCollectCommand command,
      int pageNumber,
      int numberOfRows) {
    builder
        .pathSegment(properties.endpoint())
        .queryParam("serviceKey", decodeServiceKey(properties.serviceKey()))
        .queryParam("key", command.commercialAreaCode())
        .queryParam("numOfRows", numberOfRows)
        .queryParam("pageNo", pageNumber)
        .queryParam("type", properties.type());
    addQueryParam(builder, "indsLclsCd", command.largeIndustryCode());
    addQueryParam(builder, "indsMclsCd", command.mediumIndustryCode());
    addQueryParam(builder, "indsSclsCd", command.smallIndustryCode());
    return builder;
  }

  private void addQueryParam(UriBuilder builder, String name, String value) {
    if (value != null && !value.isBlank()) {
      builder.queryParam(name, value);
    }
  }

  private void validate(SmallBusinessStoreApiResponse response) {
    if (response == null || response.header() == null) {
      throw new ApplicationException(ErrorCode.SMALL_BUSINESS_STORE_API_INVALID_RESPONSE);
    }
    if (!SUCCESS_CODE.equals(response.header().resultCode())) {
      log.warn(
          "소상공인 상가정보 OpenAPI 오류 resultCode={} resultMsg={}",
          response.header().resultCode(),
          response.header().resultMsg());
      throw new ApplicationException(ErrorCode.SMALL_BUSINESS_STORE_API_REQUEST_FAILED);
    }
    if (response.body() == null || response.body().totalCount() == null) {
      throw new ApplicationException(ErrorCode.SMALL_BUSINESS_STORE_API_INVALID_RESPONSE);
    }
  }

  private boolean isNoData(SmallBusinessStoreApiResponse response) {
    return response != null
        && response.header() != null
        && NO_DATA_CODE.equals(response.header().resultCode());
  }

  static String decodeServiceKey(String serviceKey) {
    // 공공데이터포털의 인코딩키는 먼저 복원해 RestClient가 한 번만 인코딩하게 한다.
    return serviceKey.contains("%")
        ? URLDecoder.decode(serviceKey, StandardCharsets.UTF_8)
        : serviceKey;
  }

  private SimpleClientHttpRequestFactory requestFactory() {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(CONNECT_TIMEOUT);
    factory.setReadTimeout(READ_TIMEOUT);
    return factory;
  }

  private SmallBusinessStoreRowResult toResult(SmallBusinessStoreApiResponse.Item item) {
    return new SmallBusinessStoreRowResult(
        item.bizesId(),
        item.bizesNm(),
        item.brchNm(),
        item.indsLclsCd(),
        item.indsLclsNm(),
        item.indsMclsCd(),
        item.indsMclsNm(),
        item.indsSclsCd(),
        item.indsSclsNm(),
        item.ksicCd(),
        item.ksicNm(),
        item.ctprvnCd(),
        item.ctprvnNm(),
        item.signguCd(),
        item.signguNm(),
        item.adongCd(),
        item.adongNm(),
        item.ldongCd(),
        item.ldongNm(),
        item.lnoAdr(),
        item.rdnmAdr(),
        item.bldMngNo(),
        item.bldNm(),
        item.flrNo(),
        item.hoNo(),
        item.lon(),
        item.lat());
  }
}
