package seed.seedplusbackend.global.cache;

import java.time.Duration;
import java.util.Optional;

public interface CacheStore {

  void put(CacheSpec spec, String key, Object value);

  default void put(CacheSpec spec, String key, Object value, Duration ttl) {
    put(spec, key, value);
  }

  <T> Optional<T> get(CacheSpec spec, String key, Class<T> type);

  void evict(CacheSpec spec, String key);
}
