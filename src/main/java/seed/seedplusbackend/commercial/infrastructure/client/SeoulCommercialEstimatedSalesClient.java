package seed.seedplusbackend.commercial.infrastructure.client;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import seed.seedplusbackend.commercial.application.port.SeoulCommercialEstimatedSalesClientPort;
import seed.seedplusbackend.commercial.application.result.CommercialEstimatedSalesPageResult;
import seed.seedplusbackend.commercial.application.result.CommercialEstimatedSalesRowResult;

@Component
@RequiredArgsConstructor
public class SeoulCommercialEstimatedSalesClient
    implements SeoulCommercialEstimatedSalesClientPort {

  private final SeoulCommercialOpenApiProperties properties;

  @Override
  public CommercialEstimatedSalesPageResult fetchByQuarter(
      String stdrYyquCd, int startIndex, int endIndex) {
    SeoulCommercialEstimatedSalesApiResponse response =
        RestClient.builder()
            .baseUrl(properties.baseUrl())
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
            .body(SeoulCommercialEstimatedSalesApiResponse.class);

    if (response == null || response.body() == null) {
      return new CommercialEstimatedSalesPageResult(0, Collections.emptyList());
    }

    List<CommercialEstimatedSalesRowResult> rows =
        response.body().rows() == null
            ? Collections.emptyList()
            : response.body().rows().stream().map(this::toResult).toList();

    int totalCount = response.body().totalCount() == null ? 0 : response.body().totalCount();

    return new CommercialEstimatedSalesPageResult(totalCount, rows);
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
