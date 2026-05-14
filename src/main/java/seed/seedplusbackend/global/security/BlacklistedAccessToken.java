package seed.seedplusbackend.global.security;

import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BlacklistedAccessToken {

  private final String jti;
  private final OffsetDateTime expiresAt;

  public boolean isExpired(OffsetDateTime now) {
    return !expiresAt.isAfter(now);
  }
}
