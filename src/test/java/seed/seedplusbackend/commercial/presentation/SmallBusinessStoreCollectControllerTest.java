package seed.seedplusbackend.commercial.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import seed.seedplusbackend.commercial.application.CommercialDataCollectService;
import seed.seedplusbackend.commercial.application.command.CommercialDataCollectCommand;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.global.error.GlobalExceptionHandler;

@ExtendWith(MockitoExtension.class)
@DisplayName("소상공인 상가정보 수동 수집 API")
class SmallBusinessStoreCollectControllerTest {

  private MockMvc mockMvc;

  @Mock private CommercialDataCollectService collectService;

  @BeforeEach
  void setUp() {
    LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
    validator.afterPropertiesSet();
    mockMvc =
        MockMvcBuilders.standaloneSetup(new SmallBusinessStoreCollectController(collectService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .setValidator(validator)
            .build();
  }

  @Test
  @DisplayName("외부 API 호출에 실패하면 502 응답을 반환한다")
  void collect_returnsBadGateway_whenApiCallFails() throws Exception {
    given(collectService.collect(any(CommercialDataCollectCommand.class)))
        .willThrow(new ApplicationException(ErrorCode.SMALL_BUSINESS_STORE_API_REQUEST_FAILED));

    mockMvc
        .perform(
            post("/api/v1/small-business-stores/collect")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequest()))
        .andExpect(status().isBadGateway())
        .andExpect(jsonPath("$.status").value("BAD_GATEWAY"))
        .andExpect(jsonPath("$.code").value(9300))
        .andExpect(jsonPath("$.message").value("소상공인 상가정보 OpenAPI 요청에 실패했습니다."));

    verify(collectService)
        .collect(
            argThat(
                command -> "9151".equals(command.targetKey().split(":")[0]) && !command.force()));
  }

  private String validRequest() {
    return """
        {
          "commercialAreaCode": "9151",
          "largeIndustryCode": "Q",
          "mediumIndustryCode": "Q12",
          "smallIndustryCode": "Q12A01",
          "force": false
        }
        """;
  }
}
