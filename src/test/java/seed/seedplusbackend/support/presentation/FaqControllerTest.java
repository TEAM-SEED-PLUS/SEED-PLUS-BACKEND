package seed.seedplusbackend.support.presentation;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import seed.seedplusbackend.global.error.GlobalExceptionHandler;
import seed.seedplusbackend.support.application.FaqQueryService;
import seed.seedplusbackend.support.domain.entity.Faq;
import seed.seedplusbackend.support.domain.entity.FaqCategory;
import seed.seedplusbackend.support.domain.entity.FaqGroup;

@ExtendWith(MockitoExtension.class)
@DisplayName("FAQ API")
class FaqControllerTest {

  private MockMvc mockMvc;

  @Mock private FaqQueryService faqQueryService;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(new FaqController(faqQueryService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @Test
  @DisplayName("그룹과 카테고리 조건으로 FAQ를 조회한다")
  void getFaqs_forwardsFilters() throws Exception {
    Faq faq =
        new Faq(
            16L, FaqGroup.PARTNER, FaqCategory.COOPERATION, "제휴는 어떻게 하나요?", "문의하기를 이용해 주세요.", 1);
    given(faqQueryService.getFaqs(FaqGroup.PARTNER, FaqCategory.COOPERATION, 0, 20))
        .willReturn(new PageImpl<>(List.of(faq), PageRequest.of(0, 20), 1));

    mockMvc
        .perform(get("/api/v1/faqs").param("group", "PARTNER").param("category", "COOPERATION"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(200))
        .andExpect(jsonPath("$.data.content[0].id").value(16))
        .andExpect(jsonPath("$.data.content[0].group").value("PARTNER"));

    verify(faqQueryService).getFaqs(FaqGroup.PARTNER, FaqCategory.COOPERATION, 0, 20);
  }
}
