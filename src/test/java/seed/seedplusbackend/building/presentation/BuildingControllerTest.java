package seed.seedplusbackend.building.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import seed.seedplusbackend.building.application.BuildingCommandService;
import seed.seedplusbackend.building.application.BuildingQueryService;
import seed.seedplusbackend.building.application.command.CreateBuildingCommand;
import seed.seedplusbackend.building.domain.entity.Building;
import seed.seedplusbackend.global.error.GlobalExceptionHandler;
import seed.seedplusbackend.support.fixture.BuildingFixture;
import seed.seedplusbackend.support.fixture.CommercialAreaFixture;
import seed.seedplusbackend.support.fixture.RegionFixture;

@ExtendWith(MockitoExtension.class)
@DisplayName("BuildingController")
class BuildingControllerTest {

  private MockMvc mockMvc;

  @Mock private BuildingQueryService buildingQueryService;
  @Mock private BuildingCommandService buildingCommandService;

  @BeforeEach
  void setUp() {
    LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
    validator.afterPropertiesSet();
    mockMvc =
        MockMvcBuilders.standaloneSetup(
                new BuildingController(buildingQueryService, buildingCommandService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .setValidator(validator)
            .build();
  }

  @Test
  @DisplayName("create building returns 201 and response body")
  void createBuilding_returnsCreated() throws Exception {
    Building building = building(100L);
    given(buildingCommandService.create(any(CreateBuildingCommand.class))).willReturn(building);

    mockMvc
        .perform(
            post("/api/v1/buildings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "regionId": 1,
                      "commercialAreaId": 2,
                      "address": "123 Teheran-ro",
                      "name": "Seed Building",
                      "totalFloor": 15,
                      "totalArea": 12345.67,
                      "latitude": 37.5012,
                      "longitude": 127.0364
                    }
                    """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value(201))
        .andExpect(jsonPath("$.data.buildingId").value(100))
        .andExpect(jsonPath("$.data.latitude").value(37.5012))
        .andExpect(jsonPath("$.data.longitude").value(127.0364));

    verify(buildingCommandService).create(any(CreateBuildingCommand.class));
  }

  @Test
  @DisplayName("create building returns 400 when required field is missing")
  void createBuilding_returnsBadRequest_whenRequiredFieldMissing() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/buildings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "commercialAreaId": 2,
                      "address": "123 Teheran-ro"
                    }
                    """))
        .andExpect(status().isBadRequest());

    verify(buildingCommandService, never()).create(any());
  }

  @Test
  @DisplayName("create building returns 400 when location is incomplete")
  void createBuilding_returnsBadRequest_whenLocationIsIncomplete() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/buildings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "regionId": 1,
                      "commercialAreaId": 2,
                      "address": "123 Teheran-ro",
                      "latitude": 37.5012
                    }
                    """))
        .andExpect(status().isBadRequest());

    verify(buildingCommandService, never()).create(any());
  }

  private Building building(Long id) {
    Building building =
        BuildingFixture.seoulGangnamBuilding(
            RegionFixture.seoulGangnamYeoksamLegalDong(),
            CommercialAreaFixture.developedActive("Gangnam"));
    ReflectionTestUtils.setField(building, "id", id);
    return building;
  }
}
