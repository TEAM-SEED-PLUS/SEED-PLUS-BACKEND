package seed.seedplusbackend.commercial.application.port;

import seed.seedplusbackend.commercial.application.result.CommercialEstimatedSalesPageResult;

public interface SeoulCommercialEstimatedSalesClientPort {

  CommercialEstimatedSalesPageResult fetchByQuarter(
      String stdrYyquCd, int startIndex, int endIndex);
}
