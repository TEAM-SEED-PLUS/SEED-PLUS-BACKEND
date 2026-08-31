package seed.seedplusbackend.global.cache;

import java.time.Duration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CacheSpec {
  ACCESS_TOKEN_BLACKLIST(Duration.ofDays(7), Long.MAX_VALUE);

  private final Duration expireAfterWrite;
  private final long maximumSize;
}
