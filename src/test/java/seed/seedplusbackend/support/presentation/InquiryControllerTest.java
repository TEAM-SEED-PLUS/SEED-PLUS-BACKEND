package seed.seedplusbackend.support.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import seed.seedplusbackend.global.error.GlobalExceptionHandler;
import seed.seedplusbackend.support.application.InquiryService;
import seed.seedplusbackend.support.application.command.CreateInquiryCommand;

@ExtendWith(MockitoExtension.class)
@DisplayName("문의하기 API")
class InquiryControllerTest {

  private MockMvc mockMvc;

  @Mock private InquiryService inquiryService;

  @BeforeEach
  void setUp() {
    LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
    validator.afterPropertiesSet();
    mockMvc =
        MockMvcBuilders.standaloneSetup(new InquiryController(inquiryService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .setValidator(validator)
            .build();
  }

  @Test
  @DisplayName("유효한 문의를 메일 발송 서비스로 전달한다")
  void createInquiry_forwardsCommand() throws Exception {
    String request =
        """
        {
          "type": "SERVICE_ERROR",
          "name": "홍길동",
          "email": "user@example.com",
          "title": "오류 문의",
          "content": "분석 결과가 보이지 않습니다.",
          "personalInformationConsent": true
        }
        """;

    mockMvc
        .perform(post("/api/v1/inquiries").contentType(MediaType.APPLICATION_JSON).content(request))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(200))
        .andExpect(jsonPath("$.code").value(2000));

    ArgumentCaptor<CreateInquiryCommand> commandCaptor =
        ArgumentCaptor.forClass(CreateInquiryCommand.class);
    verify(inquiryService).createInquiry(commandCaptor.capture());
    CreateInquiryCommand command = commandCaptor.getValue();
    assertThat(command.getEmail()).isEqualTo("user@example.com");
    assertThat(command.getTitle()).isEqualTo("오류 문의");
  }

  @Test
  @DisplayName("개인정보 처리에 동의하지 않으면 문의를 접수하지 않는다")
  void createInquiry_rejectsMissingPersonalInformationConsent() throws Exception {
    String request =
        """
        {
          "type": "SERVICE_USE",
          "name": "홍길동",
          "email": "user@example.com",
          "title": "서비스 문의",
          "content": "문의 내용입니다.",
          "personalInformationConsent": false
        }
        """;

    mockMvc
        .perform(post("/api/v1/inquiries").contentType(MediaType.APPLICATION_JSON).content(request))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(2001));

    verify(inquiryService, never()).createInquiry(any());
  }
}
