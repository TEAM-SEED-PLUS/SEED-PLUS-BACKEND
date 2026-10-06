package seed.seedplusbackend.weatherfeed.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import seed.seedplusbackend.global.error.GlobalExceptionHandler;
import seed.seedplusbackend.weatherfeed.application.WeatherFeedService;

@ExtendWith(MockitoExtension.class)
@DisplayName("상권 날씨 API")
class WeatherFeedControllerTest {

  private MockMvc mockMvc;

  @Mock private WeatherFeedService weatherFeedService;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp() {
    LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
    validator.afterPropertiesSet();
    mockMvc =
        MockMvcBuilders.standaloneSetup(new WeatherFeedController(weatherFeedService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .setValidator(validator)
            .build();
  }

  @Test
  @DisplayName("요약 응답을 공통 응답의 data에 담아 반환한다")
  void getOverview_wrapsFastApiResponse() throws Exception {
    given(weatherFeedService.getOverview(null, null, "아침"))
        .willReturn(objectMapper.readTree("{\"status\":\"ok\",\"districts\":[]}"));

    mockMvc
        .perform(get("/api/v1/weather-feeds/overview").param("time_band", "아침"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(200))
        .andExpect(jsonPath("$.code").value(2000))
        .andExpect(jsonPath("$.data.status").value("ok"));

    verify(weatherFeedService).getOverview(null, null, "아침");
  }

  @Test
  @DisplayName("상세 조회 파라미터를 서비스로 전달한다")
  void getDetail_forwardsQueryParameters() throws Exception {
    given(weatherFeedService.getDetail("강남구", "2026-09-30", "18:30", "저녁"))
        .willReturn(objectMapper.readTree("{\"opportunity_score\":64}"));

    mockMvc
        .perform(
            get("/api/v1/weather-feeds")
                .param("district", "강남구")
                .param("date", "2026-09-30")
                .param("time", "18:30")
                .param("time_band", "저녁"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.opportunity_score").value(64));

    verify(weatherFeedService).getDetail("강남구", "2026-09-30", "18:30", "저녁");
  }

  @Test
  @DisplayName("인터페이스에서 상속한 시간대 검증 조건을 적용한다")
  void validatesInheritedTimeBandConstraint() throws Exception {
    WeatherFeedController controller = new WeatherFeedController(weatherFeedService);
    Method method =
        WeatherFeedController.class.getMethod(
            "getOverview", String.class, String.class, String.class);

    var violations =
        Validation.buildDefaultValidatorFactory()
            .getValidator()
            .forExecutables()
            .validateParameters(controller, method, new Object[] {null, null, "새벽"});

    assertThat(violations).hasSize(1);
  }
}
