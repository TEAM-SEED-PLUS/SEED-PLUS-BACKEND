package seed.seedplusbackend.commercial.application.port;

import seed.seedplusbackend.commercial.application.result.RebSmallRetailRentFileResult;

public interface RebSmallRetailRentFileReaderPort {

  RebSmallRetailRentFileResult read(byte[] fileContent);
}
