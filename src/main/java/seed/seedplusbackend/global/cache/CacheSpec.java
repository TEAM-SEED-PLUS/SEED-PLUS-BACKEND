package seed.seedplusbackend.global.cache;

import java.time.Duration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CacheSpec {
  ACCESS_TOKEN_BLACKLIST(Duration.ofDays(7), Long.MAX_VALUE),
  ANALYSIS_PROFIT_RESULT(Duration.ofMinutes(30), 10_000),
  ANALYSIS_SURVIVAL_RESULT(Duration.ofMinutes(30), 10_000);

  private final Duration expireAfterWrite;
  private final long maximumSize;
}
