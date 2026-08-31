package seed.seedplusbackend.commercial.application.port;

import seed.seedplusbackend.commercial.application.result.SeoulSdotFootTrafficPageResult;

public interface SeoulSdotFootTrafficClientPort {
  SeoulSdotFootTrafficPageResult fetch(int startIndex, int endIndex);
}
