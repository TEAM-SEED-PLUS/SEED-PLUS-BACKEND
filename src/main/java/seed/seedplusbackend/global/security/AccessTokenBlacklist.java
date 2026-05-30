package seed.seedplusbackend.global.security;

import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import seed.seedplusbackend.global.cache.CacheSpec;
import seed.seedplusbackend.global.cache.CacheStore;

@Component
@RequiredArgsConstructor
public class AccessTokenBlacklist {

  private final CacheStore cacheStore;

  /*
   * Caffeine is a local in-memory cache. In v1 this blacklist only works within a single server
   * instance. Entries disappear when the server restarts, and blacklist state is not shared across
   * multiple running instances. Replace CacheStore with Redis or another shared store before
   * running this feature in a multi-instance environment.
   */
  public void blacklist(String jti, OffsetDateTime expiresAt) {
    cacheStore.put(
        CacheSpec.ACCESS_TOKEN_BLACKLIST, jti, new BlacklistedAccessToken(jti, expiresAt));
  }

  public boolean contains(String jti) {
    return cacheStore
        .get(CacheSpec.ACCESS_TOKEN_BLACKLIST, jti, BlacklistedAccessToken.class)
        .map(this::isActive)
        .orElse(false);
  }

  private boolean isActive(BlacklistedAccessToken token) {
    if (!token.isExpired(OffsetDateTime.now())) {
      return true;
    }

    cacheStore.evict(CacheSpec.ACCESS_TOKEN_BLACKLIST, token.getJti());
    return false;
  }
}
