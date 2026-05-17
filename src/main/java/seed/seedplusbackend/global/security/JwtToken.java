package seed.seedplusbackend.global.security;

import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JwtToken {

  private final String value;
  private final String jti;
  private final OffsetDateTime expiresAt;
  private final long expiresInMillis;
}
