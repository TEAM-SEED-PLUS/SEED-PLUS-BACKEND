package seed.seedplusbackend.commercial.application.port;

import java.util.List;
import seed.seedplusbackend.commercial.application.result.KosisBusinessCountRowResult;

public interface KosisBusinessCountStorePort {

  void upsertAll(List<KosisBusinessCountRowResult> rows);
}
