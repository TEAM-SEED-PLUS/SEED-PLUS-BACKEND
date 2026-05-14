package seed.seedplusbackend.auth.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import seed.seedplusbackend.auth.application.AuthTokenResult;

@Getter
@Builder
@Schema(description = "토큰 응답")
public class TokenResponse {

  @Schema(description = "Access Token", example = "eyJhbGciOiJIUzI1NiJ9...")
  private final String accessToken;

  @Schema(description = "토큰 타입", example = "Bearer")
  private final String tokenType;

  @Schema(description = "Access Token 만료까지 남은 시간(ms)", example = "86400000")
  private final long expiresIn;

  public static TokenResponse from(AuthTokenResult result) {
    return TokenResponse.builder()
        .accessToken(result.getAccessToken())
        .tokenType("Bearer")
        .expiresIn(result.getAccessTokenExpiresIn())
        .build();
  }
}
