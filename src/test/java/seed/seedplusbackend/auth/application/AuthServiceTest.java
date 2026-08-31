package seed.seedplusbackend.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import seed.seedplusbackend.auth.application.command.LoginCommand;
import seed.seedplusbackend.auth.application.command.PasswordResetCommand;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService")
class AuthServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private RefreshTokenRepository refreshTokenRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private JwtTokenProvider jwtTokenProvider;
  @Mock private AccessTokenBlacklist accessTokenBlacklist;
  @InjectMocks private AuthService authService;

  @Test
  @DisplayName("로그인 ID, 이메일, 휴대폰 번호로 회원가입하면 비밀번호를 암호화해 사용자를 저장한다")
  void signup_savesUserWithEncodedPassword() {
    SignupCommand command =
        new SignupCommand(
            "01012345678",
            "seedplus01",
            "seedplus@example.com",
            "password123",
            "홍길동",
            LocalDate.of(1990, 1, 1));
    given(userRepository.existsByLoginId(command.getLoginId())).willReturn(false);
    given(userRepository.existsByEmail(command.getEmail())).willReturn(false);
    given(userRepository.existsByPhoneNumber(command.getPhoneNumber())).willReturn(false);
    given(passwordEncoder.encode(command.getPassword())).willReturn("encoded-password");
    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

    authService.signup(command);

    verify(userRepository).save(userCaptor.capture());
    User saved = userCaptor.getValue();
    assertThat(saved.getLoginId()).isEqualTo(command.getLoginId());
    assertThat(saved.getEmail()).isEqualTo(command.getEmail());
    assertThat(saved.getPhoneNumber()).isEqualTo(command.getPhoneNumber());
    assertThat(saved.getBirthDate()).isEqualTo(command.getBirthDate());
    assertThat(saved.getPassword()).isEqualTo("encoded-password");
    assertThat(saved.getRole()).isEqualTo(UserRole.GENERAL);
    assertThat(saved.getStatus()).isEqualTo(UserStatus.ACTIVE);
  }

  @Test
  @DisplayName("중복된 로그인 ID로 회원가입하면 예외가 발생한다")
  void signup_throwsException_whenLoginIdDuplicated() {
    SignupCommand command = signupCommand();
    given(userRepository.existsByLoginId(command.getLoginId())).willReturn(true);

    assertThatThrownBy(() -> authService.signup(command))
        .isInstanceOf(ApplicationException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.DUPLICATE_LOGIN_ID);
  }

  @Test
  @DisplayName("중복된 이메일로 회원가입하면 예외가 발생한다")
  void signup_throwsException_whenEmailDuplicated() {
    SignupCommand command = signupCommand();
    given(userRepository.existsByLoginId(command.getLoginId())).willReturn(false);
    given(userRepository.existsByEmail(command.getEmail())).willReturn(true);

    assertThatThrownBy(() -> authService.signup(command))
        .isInstanceOf(ApplicationException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.DUPLICATE_EMAIL);
  }

  @Test
  @DisplayName("중복된 휴대폰 번호로 회원가입하면 예외가 발생한다")
  void signup_throwsException_whenPhoneNumberDuplicated() {
    SignupCommand command = signupCommand();
    given(userRepository.existsByLoginId(command.getLoginId())).willReturn(false);
    given(userRepository.existsByEmail(command.getEmail())).willReturn(false);
    given(userRepository.existsByPhoneNumber(command.getPhoneNumber())).willReturn(true);

    assertThatThrownBy(() -> authService.signup(command))
        .isInstanceOf(ApplicationException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.DUPLICATE_PHONE_NUMBER);
  }

  @Test
  @DisplayName("로그인에 성공하면 액세스 토큰과 리프레시 토큰을 발급하고 저장한다")
  void login_returnsTokensAndSavesRefreshToken_whenCredentialValid() {
    User user = activeUser();
    given(userRepository.findByLoginId(user.getLoginId())).willReturn(Optional.of(user));
    given(passwordEncoder.matches("password123", user.getPassword())).willReturn(true);
    given(jwtTokenProvider.generateAccessToken(user)).willReturn(jwtToken("access-token"));
    given(jwtTokenProvider.generateRefreshToken(user)).willReturn(jwtToken("refresh-token"));
    ArgumentCaptor<RefreshToken> refreshTokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);

    AuthTokenResult result = authService.login(new LoginCommand(user.getLoginId(), "password123"));

    assertThat(result.getAccessToken()).isEqualTo("access-token");
    verify(refreshTokenRepository).save(refreshTokenCaptor.capture());
    assertThat(refreshTokenCaptor.getValue().getTokenHash())
        .isEqualTo(TokenHashUtil.sha256("refresh-token"));
  }

  @Test
  @DisplayName("ACTIVE가 아닌 사용자는 로그인할 수 없다")
  void login_throwsException_whenUserStatusIsNotActive() {
    User user = user(UserStatus.INACTIVE);
    given(userRepository.findByLoginId(user.getLoginId())).willReturn(Optional.of(user));
    given(passwordEncoder.matches("password123", user.getPassword())).willReturn(true);

    assertThatThrownBy(() -> authService.login(new LoginCommand(user.getLoginId(), "password123")))
        .isInstanceOf(ApplicationException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.INVALID_USER_STATUS);
  }

  @Test
  @DisplayName("이메일과 기존 비밀번호가 일치하면 새 비밀번호로 변경하고 리프레시 토큰을 폐기한다")
  void resetPassword_changesPasswordAndRevokesRefreshTokens_whenCurrentPasswordMatches() {
    User user = activeUser();
    PasswordResetCommand command =
        new PasswordResetCommand(
            user.getEmail(), "password123", "newpassword123", "newpassword123");
    given(userRepository.findByEmail(command.getEmail())).willReturn(Optional.of(user));
    given(passwordEncoder.matches(command.getCurrentPassword(), user.getPassword()))
        .willReturn(true);
    given(passwordEncoder.encode(command.getNewPassword())).willReturn("new-encoded-password");
    given(refreshTokenRepository.revokeAllByUserIdIfNotRevoked(any(), any())).willReturn(2);

    authService.resetPassword(command);

    assertThat(user.getPassword()).isEqualTo("new-encoded-password");
    verify(refreshTokenRepository).revokeAllByUserIdIfNotRevoked(any(), any());
  }

  @Test
  @DisplayName("이메일 또는 기존 비밀번호가 일치하지 않으면 비밀번호를 변경하지 않는다")
  void resetPassword_throwsException_whenCurrentPasswordDoesNotMatch() {
    User user = activeUser();
    PasswordResetCommand command =
        new PasswordResetCommand(
            user.getEmail(), "wrong-password", "newpassword123", "newpassword123");
    given(userRepository.findByEmail(command.getEmail())).willReturn(Optional.of(user));
    given(passwordEncoder.matches(command.getCurrentPassword(), user.getPassword()))
        .willReturn(false);

    assertThatThrownBy(() -> authService.resetPassword(command))
        .isInstanceOf(ApplicationException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.INVALID_CREDENTIALS);

    verify(passwordEncoder, never()).encode(command.getNewPassword());
    verify(refreshTokenRepository, never()).revokeAllByUserIdIfNotRevoked(any(), any());
  }

  @Test
  @DisplayName("새 비밀번호와 확인 값이 다르면 비밀번호를 변경하지 않는다")
  void resetPassword_throwsException_whenPasswordConfirmationDoesNotMatch() {
    User user = activeUser();
    PasswordResetCommand command =
        new PasswordResetCommand(
            user.getEmail(), "password123", "newpassword123", "differentpassword123");
    given(userRepository.findByEmail(command.getEmail())).willReturn(Optional.of(user));
    given(passwordEncoder.matches(command.getCurrentPassword(), user.getPassword()))
        .willReturn(true);

    assertThatThrownBy(() -> authService.resetPassword(command))
        .isInstanceOf(ApplicationException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.PASSWORD_CONFIRMATION_MISMATCH);

    verify(passwordEncoder, never()).encode(command.getNewPassword());
    verify(refreshTokenRepository, never()).revokeAllByUserIdIfNotRevoked(any(), any());
  }

  @Test
  @DisplayName("리프레시 토큰을 재발급하면 기존 토큰을 폐기하고 새 토큰을 저장한다")
  void reissue_revokesOldRefreshTokenAndSavesNewRefreshToken() {
    User user = activeUser();
    RefreshToken oldRefreshToken =
        RefreshToken.builder()
            .user(user)
            .tokenHash(TokenHashUtil.sha256("old-refresh-token"))
            .expiresAt(OffsetDateTime.now().plusDays(1))
            .revokedAt(null)
            .build();
    given(jwtTokenProvider.getRefreshTokenUserId("old-refresh-token")).willReturn(user.getId());
    given(refreshTokenRepository.findByTokenHash(TokenHashUtil.sha256("old-refresh-token")))
        .willReturn(Optional.of(oldRefreshToken));
    given(
            refreshTokenRepository.revokeByTokenHashIfNotRevoked(
                org.mockito.ArgumentMatchers.eq(TokenHashUtil.sha256("old-refresh-token")),
                any(OffsetDateTime.class)))
        .willReturn(1);
    given(jwtTokenProvider.generateAccessToken(user)).willReturn(jwtToken("new-access-token"));
    given(jwtTokenProvider.generateRefreshToken(user)).willReturn(jwtToken("new-refresh-token"));

    AuthTokenResult result = authService.reissue("old-refresh-token");

    assertThat(result.getAccessToken()).isEqualTo("new-access-token");
    verify(refreshTokenRepository)
        .revokeByTokenHashIfNotRevoked(
            org.mockito.ArgumentMatchers.eq(TokenHashUtil.sha256("old-refresh-token")),
            any(OffsetDateTime.class));
    verify(refreshTokenRepository).save(any(RefreshToken.class));
  }

  @Test
  @DisplayName("이미 다른 요청이 리프레시 토큰을 revoke했으면 재발급에 실패한다")
  void reissue_throwsInvalidToken_whenAtomicRevokeAffectsNoRows() {
    User user = activeUser();
    RefreshToken oldRefreshToken =
        RefreshToken.builder()
            .user(user)
            .tokenHash(TokenHashUtil.sha256("old-refresh-token"))
            .expiresAt(OffsetDateTime.now().plusDays(1))
            .revokedAt(null)
            .build();
    given(jwtTokenProvider.getRefreshTokenUserId("old-refresh-token")).willReturn(user.getId());
    given(refreshTokenRepository.findByTokenHash(TokenHashUtil.sha256("old-refresh-token")))
        .willReturn(Optional.of(oldRefreshToken));
    given(
            refreshTokenRepository.revokeByTokenHashIfNotRevoked(
                org.mockito.ArgumentMatchers.eq(TokenHashUtil.sha256("old-refresh-token")),
                any(OffsetDateTime.class)))
        .willReturn(0);

    assertThatThrownBy(() -> authService.reissue("old-refresh-token"))
        .isInstanceOf(ApplicationException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.INVALID_TOKEN);

    verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
  }

  @Test
  @DisplayName("이미 폐기된 리프레시 토큰으로 재발급하면 예외가 발생한다")
  void reissue_throwsInvalidToken_whenRefreshTokenAlreadyRevoked() {
    User user = activeUser();
    RefreshToken revokedRefreshToken =
        RefreshToken.builder()
            .user(user)
            .tokenHash(TokenHashUtil.sha256("old-refresh-token"))
            .expiresAt(OffsetDateTime.now().plusDays(1))
            .revokedAt(OffsetDateTime.now().minusMinutes(1))
            .build();
    given(jwtTokenProvider.getRefreshTokenUserId("old-refresh-token")).willReturn(user.getId());
    given(refreshTokenRepository.findByTokenHash(TokenHashUtil.sha256("old-refresh-token")))
        .willReturn(Optional.of(revokedRefreshToken));

    assertThatThrownBy(() -> authService.reissue("old-refresh-token"))
        .isInstanceOf(ApplicationException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.INVALID_TOKEN);
  }

  @Test
  @DisplayName("로그아웃하면 액세스 토큰 jti를 블랙리스트에 등록한다")
  void logout_blacklistsAccessToken() {
    AuthenticatedUser authenticatedUser =
        new AuthenticatedUser(1L, "01012345678", UserRole.GENERAL);
    given(jwtTokenProvider.getAccessTokenJti("access-token")).willReturn("jti");
    OffsetDateTime expiresAt = OffsetDateTime.now().plusMinutes(10);
    given(jwtTokenProvider.getAccessTokenExpiresAt("access-token")).willReturn(expiresAt);

    authService.logout(authenticatedUser, "access-token", null);

    verify(accessTokenBlacklist).blacklist("jti", expiresAt);
  }

  private User activeUser() {
    return user(UserStatus.ACTIVE);
  }

  private User user(UserStatus status) {
    User user =
        User.builder()
            .phoneNumber("01012345678")
            .loginId("seedplus01")
            .email("seedplus@example.com")
            .birthDate(LocalDate.of(1990, 1, 1))
            .password("encoded-password")
            .name("홍길동")
            .role(UserRole.GENERAL)
            .status(status)
            .build();
    ReflectionTestUtils.setField(user, "id", 1L);
    return user;
  }

  private SignupCommand signupCommand() {
    return new SignupCommand(
        "01012345678",
        "seedplus01",
        "seedplus@example.com",
        "password123",
        "홍길동",
        LocalDate.of(1990, 1, 1));
  }

  private JwtToken jwtToken(String value) {
    return JwtToken.builder()
        .value(value)
        .jti(value + "-jti")
        .expiresAt(OffsetDateTime.now().plusDays(1))
        .expiresInMillis(86_400_000)
        .build();
  }
}
