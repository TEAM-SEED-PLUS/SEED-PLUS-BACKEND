package seed.seedplusbackend.commercial.application.port;

import seed.seedplusbackend.commercial.application.result.SeoulRealtimeCityPopulationResult;

public interface SeoulRealtimeCityPopulationClientPort {
  SeoulRealtimeCityPopulationResult fetch(String area);
}
