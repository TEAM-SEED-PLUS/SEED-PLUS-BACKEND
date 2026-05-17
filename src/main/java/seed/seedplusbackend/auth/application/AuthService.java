package seed.seedplusbackend.auth.application;

import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
            .birthDate(command.getBirthDate())
            .role(UserRole.GENERAL)
            .status(UserStatus.ACTIVE)
            .build();

    userRepository.save(user);
    log.info("[AuthService] 회원가입 완료 사용자ID={}", user.getId());
  }

  @Transactional
  public AuthTokenResult login(LoginCommand command) {
    User user =
        userRepository
            .findByPhoneNumber(command.getPhoneNumber())
            .orElseThrow(
                () -> {
                  log.warn("[AuthService] 로그인 실패, 사유=존재하지 않는 사용자 또는 비밀번호 불일치");
                  return new ApplicationException(ErrorCode.INVALID_CREDENTIALS);
                });

    if (!passwordEncoder.matches(command.getPassword(), user.getPassword())) {
      log.warn("[AuthService] 로그인 실패, 사유=존재하지 않는 사용자 또는 비밀번호 불일치 사용자ID={}", user.getId());
      throw new ApplicationException(ErrorCode.INVALID_CREDENTIALS);
    }
    validateLoginAllowed(user);

    AuthTokenResult result = issueAndSaveTokens(user);
    log.info("[AuthService] 로그인 완료 사용자ID={}", user.getId());
    return result;
  }

  @Transactional
  public AuthTokenResult reissue(String refreshTokenValue) {
    Long userId = jwtTokenProvider.getRefreshTokenUserId(refreshTokenValue);
    String tokenHash = TokenHashUtil.sha256(refreshTokenValue);
    RefreshToken refreshToken =
        refreshTokenRepository
            .findByTokenHash(tokenHash)
            .orElseThrow(
                () -> {
                  log.warn("[AuthService] 토큰 재발급 실패, 사유=저장된 리프레시 토큰 없음 사용자ID={}", userId);
                  return new ApplicationException(ErrorCode.INVALID_TOKEN);
                });

    OffsetDateTime now = OffsetDateTime.now();
    if (refreshToken.isExpired(now)) {
      log.warn("[AuthService] 토큰 재발급 실패, 사유=리프레시 토큰 만료 사용자ID={}", userId);
      throw new ApplicationException(ErrorCode.EXPIRED_REFRESH_TOKEN);
    }
    if (refreshToken.isRevoked() || !refreshToken.getUser().getId().equals(userId)) {
      log.warn("[AuthService] 토큰 재발급 실패, 사유=폐기되었거나 사용자 정보가 일치하지 않는 리프레시 토큰 사용자ID={}", userId);
      throw new ApplicationException(ErrorCode.INVALID_TOKEN);
    }

    User user = refreshToken.getUser();
    validateLoginAllowed(user);

    int revokedCount = refreshTokenRepository.revokeByTokenHashIfNotRevoked(tokenHash, now);
    if (revokedCount != 1) {
      log.warn("[AuthService] 토큰 재발급 실패, 사유=리프레시 토큰 회전 충돌 사용자ID={}", userId);
      throw new ApplicationException(ErrorCode.INVALID_TOKEN);
    }

    AuthTokenResult result = issueAndSaveTokens(user);
    log.info("[AuthService] 토큰 재발급 완료 사용자ID={}", user.getId());
    return result;
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
    log.info(
        "[AuthService] 로그아웃 완료 사용자ID={} 리프레시토큰제공여부={}",
        authenticatedUser.getId(),
        StringUtils.hasText(refreshTokenValue));
  }

  private void validateSignup(SignupCommand command) {
    if (userRepository.existsByPhoneNumber(command.getPhoneNumber())) {
      log.warn("[AuthService] 회원가입 실패, 사유=중복된 전화번호");
      throw new ApplicationException(ErrorCode.DUPLICATE_PHONE_NUMBER);
    }
  }

  private void validateLoginAllowed(User user) {
    if (user.getStatus() != UserStatus.ACTIVE) {
      log.warn(
          "[AuthService] 로그인 실패, 사유=활성 상태가 아닌 사용자 사용자ID={} 사용자상태={}",
          user.getId(),
          user.getStatus());
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
}
