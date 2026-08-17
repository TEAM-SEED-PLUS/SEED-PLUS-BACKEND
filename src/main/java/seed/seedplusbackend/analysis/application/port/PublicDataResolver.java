package seed.seedplusbackend.analysis.application.port;

import seed.seedplusbackend.analysis.application.result.PublicDataMetrics;

public interface PublicDataResolver {
  PublicDataMetrics resolve(String regionName, String industryName);
}
