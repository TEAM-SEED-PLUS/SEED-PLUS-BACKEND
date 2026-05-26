package seed.seedplusbackend.building.presentation;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import seed.seedplusbackend.building.application.BuildingQueryService;
import seed.seedplusbackend.global.error.GlobalExceptionHandler;

@ExtendWith(MockitoExtension.class)
@DisplayName("BuildingController")
class BuildingControllerTest {

  private MockMvc mockMvc;

  @Mock private BuildingQueryService buildingQueryService;

  @BeforeEach
  void setUp() {
    LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
    validator.afterPropertiesSet();
    mockMvc =
        MockMvcBuilders.standaloneSetup(new BuildingController(buildingQueryService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .setValidator(validator)
            .build();
  }

  @Test
  @DisplayName("건물 단독 생성 API는 지원하지 않는다")
  void createBuilding_returnsMethodNotAllowed() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/buildings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "regionId": 1,
                      "commercialAreaId": 2,
                      "address": "123 Teheran-ro"
                    }
                    """))
        .andExpect(status().isMethodNotAllowed());

    verifyNoInteractions(buildingQueryService);
  }
}
