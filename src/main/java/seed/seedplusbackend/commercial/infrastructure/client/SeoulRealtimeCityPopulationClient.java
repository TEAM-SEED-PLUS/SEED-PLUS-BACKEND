package seed.seedplusbackend.commercial.infrastructure.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import seed.seedplusbackend.commercial.application.port.SeoulRealtimeCityPopulationClientPort;
import seed.seedplusbackend.commercial.application.result.SeoulRealtimeCityPopulationResult;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;

@Component
public class SeoulRealtimeCityPopulationClient implements SeoulRealtimeCityPopulationClientPort {

  private static final String SUCCESS_CODE = "INFO-000";
  private static final DateTimeFormatter POPULATION_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  private final RestClient restClient;
  private final SeoulRealtimeCityOpenApiProperties properties;

  public SeoulRealtimeCityPopulationClient(
      @Qualifier("externalRestClientBuilder") RestClient.Builder restClientBuilder,
      SeoulRealtimeCityOpenApiProperties properties) {
    this.restClient = restClientBuilder.clone().baseUrl(properties.baseUrl()).build();
    this.properties = properties;
  }

  @Override
  public SeoulRealtimeCityPopulationResult fetch(String area) {
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
                              String.valueOf(properties.startIndex()),
                              String.valueOf(properties.endIndex()),
                              area)
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

    Document document = parseXml(responseBytes);
    validateResult(document);
    return parsePopulation(document);
  }

  private Document parseXml(byte[] responseBytes) {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
      factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
      factory.setXIncludeAware(false);
      factory.setExpandEntityReferences(false);
      return factory.newDocumentBuilder().parse(new ByteArrayInputStream(responseBytes));
    } catch (ParserConfigurationException | SAXException | IOException exception) {
      throw new ApplicationException(ErrorCode.SEOUL_OPEN_API_INVALID_RESPONSE, exception);
    }
  }

  private void validateResult(Document document) {
    NodeList resultNodes = document.getElementsByTagName("RESULT");
    if (resultNodes.getLength() == 0) {
      throw new ApplicationException(ErrorCode.SEOUL_OPEN_API_INVALID_RESPONSE);
    }
    Element result = (Element) resultNodes.item(0);
    String code = childText(result, "RESULT.CODE");
    if (code == null) {
      code = childText(result, "CODE");
    }
    if (!SUCCESS_CODE.equals(code)) {
      throw new ApplicationException(ErrorCode.SEOUL_OPEN_API_REQUEST_FAILED);
    }
  }

  private SeoulRealtimeCityPopulationResult parsePopulation(Document document) {
    NodeList nodes = document.getElementsByTagName("LIVE_PPLTN_STTS");
    for (int index = 0; index < nodes.getLength(); index++) {
      Node node = nodes.item(index);
      if (node instanceof Element element && hasText(element, "AREA_PPLTN_MIN")) {
        return toResult(element);
      }
    }
    throw new ApplicationException(ErrorCode.SEOUL_OPEN_API_INVALID_RESPONSE);
  }

  private SeoulRealtimeCityPopulationResult toResult(Element element) {
    try {
      String areaCode = requiredText(element, "AREA_CD");
      String areaName = requiredText(element, "AREA_NM");
      long populationMin = parseNonNegativeLong(requiredText(element, "AREA_PPLTN_MIN"));
      long populationMax = parseNonNegativeLong(requiredText(element, "AREA_PPLTN_MAX"));
      if (populationMin > populationMax) {
        throw new IllegalArgumentException("최소 인구가 최대 인구보다 클 수 없습니다.");
      }
      return new SeoulRealtimeCityPopulationResult(
          areaCode,
          areaName,
          childText(element, "AREA_CONGEST_LVL"),
          childText(element, "AREA_CONGEST_MSG"),
          populationMin,
          populationMax,
          decimal(element, "MALE_PPLTN_RATE"),
          decimal(element, "FEMALE_PPLTN_RATE"),
          decimal(element, "PPLTN_RATE_0"),
          decimal(element, "PPLTN_RATE_10"),
          decimal(element, "PPLTN_RATE_20"),
          decimal(element, "PPLTN_RATE_30"),
          decimal(element, "PPLTN_RATE_40"),
          decimal(element, "PPLTN_RATE_50"),
          decimal(element, "PPLTN_RATE_60"),
          decimal(element, "PPLTN_RATE_70"),
          decimal(element, "RESNT_PPLTN_RATE"),
          decimal(element, "NON_RESNT_PPLTN_RATE"),
          "Y".equalsIgnoreCase(childText(element, "REPLACE_YN")),
          LocalDateTime.parse(requiredText(element, "PPLTN_TIME"), POPULATION_TIME_FORMATTER));
    } catch (IllegalArgumentException | DateTimeParseException exception) {
      throw new ApplicationException(ErrorCode.SEOUL_OPEN_API_INVALID_RESPONSE, exception);
    }
  }

  private long parseNonNegativeLong(String value) {
    long parsed = Long.parseLong(value.replace(",", ""));
    if (parsed < 0) {
      throw new IllegalArgumentException("인구 값은 음수일 수 없습니다.");
    }
    return parsed;
  }

  private BigDecimal decimal(Element element, String tagName) {
    String value = childText(element, tagName);
    return value == null ? null : new BigDecimal(value);
  }

  private String requiredText(Element element, String tagName) {
    String value = childText(element, tagName);
    if (value == null) {
      throw new IllegalArgumentException(tagName + " 값이 누락되었습니다.");
    }
    return value;
  }

  private boolean hasText(Element element, String tagName) {
    return childText(element, tagName) != null;
  }

  private String childText(Element element, String tagName) {
    NodeList nodes = element.getElementsByTagName(tagName);
    if (nodes.getLength() == 0) {
      return null;
    }
    String value = nodes.item(0).getTextContent();
    return value == null || value.isBlank() ? null : value.trim();
  }
}
