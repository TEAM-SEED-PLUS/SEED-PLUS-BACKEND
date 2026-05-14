package seed.seedplusbackend.global.cache;

import java.time.Duration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CacheSpec {
  ACCESS_TOKEN_BLACKLIST(resolveAccessTokenBlacklistExpiry(), Long.MAX_VALUE);

  private final Duration expireAfterWrite;
  private final long maximumSize;

  private static Duration resolveAccessTokenBlacklistExpiry() {
    String configuredExpireMs = System.getProperty("JWT_ACCESS_EXPIRE_MS");
    if (configuredExpireMs == null || configuredExpireMs.isBlank()) {
      configuredExpireMs = System.getenv("JWT_ACCESS_EXPIRE_MS");
    }

    if (configuredExpireMs == null || configuredExpireMs.isBlank()) {
      return Duration.ofDays(7);
    }

    try {
      long expireMs = Long.parseLong(configuredExpireMs);
      if (expireMs > 0) {
        return Duration.ofMillis(expireMs);
      }
    } catch (NumberFormatException ignored) {
      // Fallback to the previous default when the configured value is invalid.
    }

    return Duration.ofDays(7);
  }
}
