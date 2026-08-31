package seed.seedplusbackend.commercial.application.port;

import java.util.Optional;

public interface CommercialDataCollectClaimPort {

  Optional<Long> tryClaim(String dataType, String targetKey, boolean force);
}
