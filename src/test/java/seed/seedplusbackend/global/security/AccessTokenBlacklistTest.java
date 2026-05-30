package seed.seedplusbackend.global.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import seed.seedplusbackend.global.cache.CaffeineCacheStore;

@DisplayName("AccessTokenBlacklist")
class AccessTokenBlacklistTest {

  @Test
  @DisplayName("블랙리스트에 등록된 jti는 조회할 수 있다")
  void contains_returnsTrue_whenJtiIsBlacklisted() {
    AccessTokenBlacklist blacklist = new AccessTokenBlacklist(new CaffeineCacheStore());

    blacklist.blacklist("jti", OffsetDateTime.now().plusMinutes(10));

    assertThat(blacklist.contains("jti")).isTrue();
  }

  @Test
  @DisplayName("만료된 블랙리스트 항목은 조회되지 않는다")
  void contains_returnsFalse_whenBlacklistedTokenExpired() {
    AccessTokenBlacklist blacklist = new AccessTokenBlacklist(new CaffeineCacheStore());

    blacklist.blacklist("expired-jti", OffsetDateTime.now().minusSeconds(1));

    assertThat(blacklist.contains("expired-jti")).isFalse();
  }
}
