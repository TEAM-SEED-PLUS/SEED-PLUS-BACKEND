package seed.seedplusbackend.auth.application;

import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import seed.seedplusbackend.auth.application.command.LoginCommand;
import seed.seedplusbackend.auth.application.command.SignupCommand;
import seed.seedplusbackend.auth.domain.entity.RefreshToken;
import seed.seedplusbackend.auth.domain.repository.RefreshTokenRepository;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.global.security.AccessTokenBlacklist;
import seed.seedplusbackend.global.security.AuthenticatedUser;
import seed.seedplusbackend.global.security.JwtToken;
import seed.seedplusbackend.global.security.JwtTokenProvider;
import seed.seedplusbackend.global.security.TokenHashUtil;
import seed.seedplusbackend.user.domain.entity.User;
import seed.seedplusbackend.user.domain.entity.UserRole;
import seed.seedplusbackend.user.domain.entity.UserStatus;
import seed.seedplusbackend.user.domain.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;
  private final AccessTokenBlacklist accessTokenBlacklist;

  @Transactional
  public void signup(SignupCommand command) {
    validateSignup(command);

    User user =
        User.builder()
            .phoneNumber(command.getPhoneNumber())
            .password(passwordEncoder.encode(command.getPassword()))
            .name(command.getName())
            .email(normalizeEmail(command.getEmail()))
            .role(UserRole.GENERAL)
            .status(UserStatus.ACTIVE)
            .build();

    userRepository.save(user);
  }

  @Transactional
  public AuthTokenResult login(LoginCommand command) {
    User user =
        userRepository
            .findByPhoneNumber(command.getPhoneNumber())
            .orElseThrow(() -> new ApplicationException(ErrorCode.INVALID_CREDENTIALS));

    if (!passwordEncoder.matches(command.getPassword(), user.getPassword())) {
      throw new ApplicationException(ErrorCode.INVALID_CREDENTIALS);
    }
    validateLoginAllowed(user);

    return issueAndSaveTokens(user);
  }

  @Transactional
  public AuthTokenResult reissue(String refreshTokenValue) {
    Long userId = jwtTokenProvider.getRefreshTokenUserId(refreshTokenValue);
    String tokenHash = TokenHashUtil.sha256(refreshTokenValue);
    RefreshToken refreshToken =
        refreshTokenRepository
            .findByTokenHashForUpdate(tokenHash)
            .orElseThrow(() -> new ApplicationException(ErrorCode.INVALID_TOKEN));

    OffsetDateTime now = OffsetDateTime.now();
    if (refreshToken.isExpired(now)) {
      throw new ApplicationException(ErrorCode.EXPIRED_REFRESH_TOKEN);
    }
    if (refreshToken.isRevoked() || !refreshToken.getUser().getId().equals(userId)) {
      throw new ApplicationException(ErrorCode.INVALID_TOKEN);
    }

    User user = refreshToken.getUser();
    validateLoginAllowed(user);
    refreshToken.revoke(now);

    return issueAndSaveTokens(user);
  }

  @Transactional
  public void logout(
      AuthenticatedUser authenticatedUser, String accessTokenValue, String refreshTokenValue) {
    if (StringUtils.hasText(refreshTokenValue)) {
      revokeRefreshTokenIfOwned(authenticatedUser.getId(), refreshTokenValue);
    }

    String jti = jwtTokenProvider.getAccessTokenJti(accessTokenValue);
    OffsetDateTime expiresAt = jwtTokenProvider.getAccessTokenExpiresAt(accessTokenValue);
    accessTokenBlacklist.blacklist(jti, expiresAt);
  }

  private void validateSignup(SignupCommand command) {
    if (userRepository.existsByPhoneNumber(command.getPhoneNumber())) {
      throw new ApplicationException(ErrorCode.DUPLICATE_PHONE_NUMBER);
    }

    String email = normalizeEmail(command.getEmail());
    if (StringUtils.hasText(email) && userRepository.existsByEmail(email)) {
      throw new ApplicationException(ErrorCode.DUPLICATE_EMAIL);
    }
  }

  private void validateLoginAllowed(User user) {
    if (user.getStatus() != UserStatus.ACTIVE) {
      throw new ApplicationException(ErrorCode.INVALID_USER_STATUS);
    }
  }

  private AuthTokenResult issueAndSaveTokens(User user) {
    JwtToken accessToken = jwtTokenProvider.generateAccessToken(user);
    JwtToken refreshToken = jwtTokenProvider.generateRefreshToken(user);

    refreshTokenRepository.save(
        RefreshToken.builder()
            .user(user)
            .tokenHash(TokenHashUtil.sha256(refreshToken.getValue()))
            .expiresAt(refreshToken.getExpiresAt())
            .revokedAt(null)
            .build());

    return AuthTokenResult.builder()
        .accessToken(accessToken.getValue())
        .accessTokenExpiresIn(accessToken.getExpiresInMillis())
        .refreshToken(refreshToken.getValue())
        .refreshTokenExpiresIn(refreshToken.getExpiresInMillis())
        .build();
  }

  private void revokeRefreshTokenIfOwned(Long userId, String refreshTokenValue) {
    String tokenHash = TokenHashUtil.sha256(refreshTokenValue);
    refreshTokenRepository
        .findByTokenHashForUpdate(tokenHash)
        .filter(refreshToken -> refreshToken.getUser().getId().equals(userId))
        .filter(refreshToken -> !refreshToken.isRevoked())
        .ifPresent(refreshToken -> refreshToken.revoke(OffsetDateTime.now()));
  }

  private String normalizeEmail(String email) {
    if (!StringUtils.hasText(email)) {
      return null;
    }

    return email.trim();
  }
}
