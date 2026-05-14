package seed.seedplusbackend.global.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class CaffeineCacheStore implements CacheStore {

  private final Map<CacheSpec, Cache<String, Object>> caches = new EnumMap<>(CacheSpec.class);

  public CaffeineCacheStore() {
    for (CacheSpec spec : CacheSpec.values()) {
      caches.put(
          spec,
          Caffeine.newBuilder()
              .expireAfterWrite(spec.getExpireAfterWrite())
              .maximumSize(spec.getMaximumSize())
              .build());
    }
  }

  @Override
  public void put(CacheSpec spec, String key, Object value) {
    caches.get(spec).put(key, value);
  }

  @Override
  public <T> Optional<T> get(CacheSpec spec, String key, Class<T> type) {
    Object value = caches.get(spec).getIfPresent(key);
    if (value == null || !type.isInstance(value)) {
      return Optional.empty();
    }

    return Optional.of(type.cast(value));
  }

  @Override
  public void evict(CacheSpec spec, String key) {
    caches.get(spec).invalidate(key);
  }
}
