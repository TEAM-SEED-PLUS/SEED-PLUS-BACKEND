package seed.seedplusbackend.commercial.application.port;

import seed.seedplusbackend.commercial.application.result.RebSmallRetailRentFileResult;

public interface RebSmallRetailRentStorePort {

  void replace(String sourceFileName, String sourceFileHash, RebSmallRetailRentFileResult file);
}
