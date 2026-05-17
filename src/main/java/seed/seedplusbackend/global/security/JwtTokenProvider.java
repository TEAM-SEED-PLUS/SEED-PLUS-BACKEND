package seed.seedplusbackend.global.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.user.domain.entity.User;
import seed.seedplusbackend.user.domain.entity.UserRole;

@Component
public class JwtTokenProvider {

  private static final String TOKEN_TYPE_CLAIM = "tokenType";
  private static final String ACCESS_TOKEN_TYPE = "access";
  private static final String REFRESH_TOKEN_TYPE = "refresh";
  private static final String PHONE_NUMBER_CLAIM = "phoneNumber";
  private static final String ROLE_CLAIM = "role";
  private static final String AUTHORITIES_CLAIM = "authorities";

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.access-token.expire-time}")
  private long accessTokenExpireMillis;

  @Value("${jwt.refresh-token.expire-time}")
  private long refreshTokenExpireMillis;

  private Key signingKey;

  @PostConstruct
  void initialize() {
    signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  public JwtToken generateAccessToken(User user) {
    return generateToken(user, ACCESS_TOKEN_TYPE, accessTokenExpireMillis);
  }

  public JwtToken generateRefreshToken(User user) {
    return generateToken(user, REFRESH_TOKEN_TYPE, refreshTokenExpireMillis);
  }

  public Authentication getAuthentication(String accessToken) {
    return toAuthentication(parseAccessClaims(accessToken));
  }

  public Authentication toAuthentication(Claims claims) {
    AuthenticatedUser principal =
        new AuthenticatedUser(
            parseUserId(claims),
            claims.get(PHONE_NUMBER_CLAIM, String.class),
            parseUserRole(claims));

    return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
  }

  public String getJti(Claims claims) {
    return claims.getId();
  }

  public Long getRefreshTokenUserId(String refreshToken) {
    return parseUserId(parseRefreshClaims(refreshToken));
  }

  public String getAccessTokenJti(String accessToken) {
    return parseAccessClaims(accessToken).getId();
  }

  public OffsetDateTime getAccessTokenExpiresAt(String accessToken) {
    return toOffsetDateTime(parseAccessClaims(accessToken).getExpiration().toInstant());
  }

  public long getAccessTokenExpireMillis() {
    return accessTokenExpireMillis;
  }

  public long getRefreshTokenExpireMillis() {
    return refreshTokenExpireMillis;
  }

  private JwtToken generateToken(User user, String tokenType, long expireMillis) {
    Instant issuedAt = Instant.now();
    Instant expiresAt = issuedAt.plusMillis(expireMillis);
    String jti = UUID.randomUUID().toString();

    String value =
        Jwts.builder()
            .setSubject(String.valueOf(user.getId()))
            .setId(jti)
            .setIssuedAt(Date.from(issuedAt))
            .setExpiration(Date.from(expiresAt))
            .claim(TOKEN_TYPE_CLAIM, tokenType)
            .claim(PHONE_NUMBER_CLAIM, user.getPhoneNumber())
            .claim(ROLE_CLAIM, user.getRole().name())
            .claim(AUTHORITIES_CLAIM, List.of("ROLE_" + user.getRole().name()))
            .signWith(signingKey, SignatureAlgorithm.HS256)
            .compact();

    return JwtToken.builder()
        .value(value)
        .jti(jti)
        .expiresAt(toOffsetDateTime(expiresAt))
        .expiresInMillis(expireMillis)
        .build();
  }

  public Claims parseAccessClaims(String token) {
    return parseTypedClaims(token, ACCESS_TOKEN_TYPE, ErrorCode.EXPIRED_TOKEN);
  }

  private Claims parseRefreshClaims(String token) {
    return parseTypedClaims(token, REFRESH_TOKEN_TYPE, ErrorCode.EXPIRED_REFRESH_TOKEN);
  }

  private Claims parseTypedClaims(String token, String expectedTokenType, ErrorCode expiredCode) {
    Claims claims = parseClaims(token, expiredCode);
    String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);
    if (!expectedTokenType.equals(tokenType)) {
      throw new ApplicationException(ErrorCode.INVALID_TOKEN);
    }

    return claims;
  }

  private Claims parseClaims(String token, ErrorCode expiredCode) {
    try {
      return Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token).getBody();
    } catch (ExpiredJwtException e) {
      throw new ApplicationException(expiredCode);
    } catch (JwtException | IllegalArgumentException e) {
      throw new ApplicationException(ErrorCode.INVALID_TOKEN);
    }
  }

  private Long parseUserId(Claims claims) {
    try {
      return Long.valueOf(claims.getSubject());
    } catch (NumberFormatException e) {
      throw new ApplicationException(ErrorCode.INVALID_TOKEN);
    }
  }

  private UserRole parseUserRole(Claims claims) {
    try {
      return UserRole.valueOf(claims.get(ROLE_CLAIM, String.class));
    } catch (IllegalArgumentException | NullPointerException e) {
      throw new ApplicationException(ErrorCode.INVALID_TOKEN);
    }
  }

  private OffsetDateTime toOffsetDateTime(Instant instant) {
    return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
  }
}
