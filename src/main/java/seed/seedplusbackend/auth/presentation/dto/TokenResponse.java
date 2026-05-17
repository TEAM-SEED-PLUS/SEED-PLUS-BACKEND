package seed.seedplusbackend.auth.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import seed.seedplusbackend.auth.application.AuthTokenResult;

@Schema(description = "토큰 응답")
public record TokenResponse(
    @Schema(description = "Access Token", example = "eyJhbGciOiJIUzI1NiJ9...") String accessToken,
    @Schema(description = "토큰 타입", example = "Bearer") String tokenType,
    @Schema(description = "Access Token 만료까지 남은 시간(ms)", example = "86400000") long expiresIn) {

  public static TokenResponse from(AuthTokenResult result) {
    return new TokenResponse(result.getAccessToken(), "Bearer", result.getAccessTokenExpiresIn());
  }
}
