package seed.seedplusbackend.commercial.application.port;

import java.util.List;
import seed.seedplusbackend.commercial.application.result.CommercialEstimatedSalesRowResult;

public interface CommercialEstimatedSalesStorePort {

  void upsertAll(List<CommercialEstimatedSalesRowResult> rows);
}
