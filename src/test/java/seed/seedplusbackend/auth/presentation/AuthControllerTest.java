package seed.seedplusbackend.auth.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import seed.seedplusbackend.auth.application.AuthService;
import seed.seedplusbackend.auth.application.AuthTokenResult;
import seed.seedplusbackend.auth.application.command.LoginCommand;
import seed.seedplusbackend.auth.application.command.PasswordResetCommand;
import seed.seedplusbackend.auth.application.command.SignupCommand;
import seed.seedplusbackend.auth.application.command.TemporaryPasswordIssueCommand;
import seed.seedplusbackend.global.error.GlobalExceptionHandler;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController")
class AuthControllerTest {

  private MockMvc mockMvc;

  @Mock private AuthService authService;
  @Mock private RefreshTokenCookieManager refreshTokenCookieManager;

  @BeforeEach
  void setUp() {
    LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
    validator.afterPropertiesSet();
    mockMvc =
        MockMvcBuilders.standaloneSetup(new AuthController(authService, refreshTokenCookieManager))
            .setControllerAdvice(new GlobalExceptionHandler())
            .setValidator(validator)
            .build();
  }

  @Test
  @DisplayName("회원가입에 성공하면 201 Created와 성공 응답을 반환한다")
  void signup_returnsCreatedAndApiResponse_whenRequestValid() throws Exception {
    String request =
        """
        {
          "loginId": "seedplus01",
          "email": "seedplus@example.com",
          "phoneNumber": "01012345678",
          "password": "password123",
          "name": "홍길동",
          "birthDate": "1990-01-01"
        }
        """;

    mockMvc
        .perform(
            post("/api/v1/auth/signup").contentType(MediaType.APPLICATION_JSON).content(request))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value(201))
        .andExpect(jsonPath("$.code").value(2000))
        .andExpect(jsonPath("$.message").value("요청 성공"))
        .andExpect(jsonPath("$.data").doesNotExist());

    verify(authService).signup(any(SignupCommand.class));
  }

  @Test
  @DisplayName("휴대폰 번호 형식이 올바르지 않으면 400 Bad Request를 반환한다")
  void signup_returnsBadRequest_whenPhoneNumberInvalid() throws Exception {
    String request =
        """
        {
          "loginId": "seedplus01",
          "email": "seedplus@example.com",
          "phoneNumber": "010-1234-5678",
          "password": "password123",
          "name": "홍길동",
          "birthDate": "1990-01-01"
        }
        """;

    mockMvc
        .perform(
            post("/api/v1/auth/signup").contentType(MediaType.APPLICATION_JSON).content(request))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("이메일과 기존 비밀번호가 유효하면 비밀번호 재설정 성공 응답을 반환한다")
  void resetPassword_returnsOk_whenRequestValid() throws Exception {
    String request =
        """
        {
          "email": "seedplus@example.com",
          "currentPassword": "password123",
          "newPassword": "newpassword123",
          "newPasswordConfirmation": "newpassword123"
        }
        """;
    given(refreshTokenCookieManager.deleteCookie())
        .willReturn(ResponseCookie.from("refreshToken", "").maxAge(0).build());

    mockMvc
        .perform(
            post("/api/v1/auth/password/reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(200));

    verify(authService).resetPassword(any(PasswordResetCommand.class));
  }

  @Test
  @DisplayName("가입 이메일로 임시 비밀번호 발급을 요청하면 200 OK와 성공 응답을 반환한다")
  void issueTemporaryPassword_returnsOk_whenRequestValid() throws Exception {
    String request =
        """
        {
          "email": "seedplus@example.com"
        }
        """;

    mockMvc
        .perform(
            post("/api/v1/auth/password/temporary")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(200))
        .andExpect(jsonPath("$.code").value(2000))
        .andExpect(jsonPath("$.message").value("요청 성공"))
        .andExpect(jsonPath("$.data").doesNotExist());

    verify(authService).issueTemporaryPassword(any(TemporaryPasswordIssueCommand.class));
  }

  @Test
  @DisplayName("임시 비밀번호 발급 요청의 이메일 형식이 올바르지 않으면 400 Bad Request를 반환한다")
  void issueTemporaryPassword_returnsBadRequest_whenEmailInvalid() throws Exception {
    String request =
        """
        {
          "email": "not-an-email"
        }
        """;

    mockMvc
        .perform(
            post("/api/v1/auth/password/temporary")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("임시 비밀번호 상태로 로그인하면 응답에 비밀번호 변경 필요 여부가 포함된다")
  void login_returnsPasswordChangeRequired_whenPasswordIsTemporary() throws Exception {
    String request =
        """
        {
          "loginId": "seedplus01",
          "password": "temporary123"
        }
        """;
    given(authService.login(any(LoginCommand.class)))
        .willReturn(
            AuthTokenResult.builder()
                .accessToken("access-token")
                .accessTokenExpiresIn(86_400_000)
                .refreshToken("refresh-token")
                .refreshTokenExpiresIn(259_200_000)
                .passwordChangeRequired(true)
                .build());
    given(refreshTokenCookieManager.createCookie("refresh-token", 259_200_000))
        .willReturn(ResponseCookie.from("refreshToken", "refresh-token").build());

    mockMvc
        .perform(
            post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(request))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.accessToken").value("access-token"))
        .andExpect(jsonPath("$.data.passwordChangeRequired").value(true));
  }
}
