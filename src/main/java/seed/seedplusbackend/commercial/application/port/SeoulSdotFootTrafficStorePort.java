package seed.seedplusbackend.commercial.application.port;

import java.util.List;
import seed.seedplusbackend.commercial.application.result.SeoulSdotFootTrafficRowResult;

public interface SeoulSdotFootTrafficStorePort {
  void upsertAll(List<SeoulSdotFootTrafficRowResult> rows);
}
