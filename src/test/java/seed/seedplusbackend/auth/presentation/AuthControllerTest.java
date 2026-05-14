package seed.seedplusbackend.auth.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import seed.seedplusbackend.auth.application.AuthService;
import seed.seedplusbackend.auth.application.command.SignupCommand;
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
  @DisplayName("회원가입에 성공하면 201 Created와 빈 본문을 반환한다")
  void signup_returnsCreatedAndEmptyBody_whenRequestValid() throws Exception {
    String request =
        """
        {
          "phoneNumber": "01012345678",
          "password": "password123",
          "name": "홍길동",
          "email": "user@test.com"
        }
        """;

    mockMvc
        .perform(
            post("/api/v1/auth/signup").contentType(MediaType.APPLICATION_JSON).content(request))
        .andExpect(status().isCreated())
        .andExpect(content().string(""));

    verify(authService).signup(any(SignupCommand.class));
  }

  @Test
  @DisplayName("휴대폰 번호 형식이 올바르지 않으면 400 Bad Request를 반환한다")
  void signup_returnsBadRequest_whenPhoneNumberInvalid() throws Exception {
    String request =
        """
        {
          "phoneNumber": "010-1234-5678",
          "password": "password123",
          "name": "홍길동"
        }
        """;

    mockMvc
        .perform(
            post("/api/v1/auth/signup").contentType(MediaType.APPLICATION_JSON).content(request))
        .andExpect(status().isBadRequest());
  }
}
