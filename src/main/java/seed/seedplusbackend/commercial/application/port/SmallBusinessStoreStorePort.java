package seed.seedplusbackend.commercial.application.port;

import java.util.List;
import seed.seedplusbackend.commercial.application.result.SmallBusinessStoreRowResult;

public interface SmallBusinessStoreStorePort {

  void upsertAll(String commercialAreaCode, List<SmallBusinessStoreRowResult> rows);
}
