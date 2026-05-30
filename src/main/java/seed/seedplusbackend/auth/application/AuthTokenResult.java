package seed.seedplusbackend.auth.application;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthTokenResult {

  private final String accessToken;
  private final long accessTokenExpiresIn;
  private final String refreshToken;
  private final long refreshTokenExpiresIn;
}
