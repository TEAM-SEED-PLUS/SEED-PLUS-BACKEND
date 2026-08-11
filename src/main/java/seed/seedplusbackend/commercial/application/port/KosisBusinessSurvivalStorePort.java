package seed.seedplusbackend.commercial.application.port;

import java.util.List;
import seed.seedplusbackend.commercial.application.result.KosisBusinessSurvivalRowResult;

public interface KosisBusinessSurvivalStorePort {

  void upsertAll(List<KosisBusinessSurvivalRowResult> rows);
}
