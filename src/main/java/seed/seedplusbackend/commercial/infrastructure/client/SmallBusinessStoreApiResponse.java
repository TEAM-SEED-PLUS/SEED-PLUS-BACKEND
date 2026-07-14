package seed.seedplusbackend.commercial.infrastructure.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SmallBusinessStoreApiResponse(Header header, Body body) {

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Header(String resultCode, String resultMsg) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Body(List<Item> items, Integer numOfRows, Integer pageNo, Integer totalCount) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Item(
      String bizesId,
      String bizesNm,
      String brchNm,
      String indsLclsCd,
      String indsLclsNm,
      String indsMclsCd,
      String indsMclsNm,
      String indsSclsCd,
      String indsSclsNm,
      String ksicCd,
      String ksicNm,
      String ctprvnCd,
      String ctprvnNm,
      String signguCd,
      String signguNm,
      String adongCd,
      String adongNm,
      String ldongCd,
      String ldongNm,
      String lnoAdr,
      String rdnmAdr,
      String bldMngNo,
      String bldNm,
      String flrNo,
      String hoNo,
      BigDecimal lon,
      BigDecimal lat) {}
}
