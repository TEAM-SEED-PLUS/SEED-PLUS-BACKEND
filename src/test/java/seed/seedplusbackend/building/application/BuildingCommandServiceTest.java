package seed.seedplusbackend.building.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import seed.seedplusbackend.building.application.command.CreateBuildingCommand;
import seed.seedplusbackend.building.domain.entity.Building;
import seed.seedplusbackend.building.domain.repository.BuildingRepository;
import seed.seedplusbackend.commercial.domain.repository.CommercialAreaRepository;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.region.domain.repository.RegionRepository;
import seed.seedplusbackend.support.fixture.CommercialAreaFixture;
import seed.seedplusbackend.support.fixture.RegionFixture;

@ExtendWith(MockitoExtension.class)
@DisplayName("BuildingCommandService")
class BuildingCommandServiceTest {

  @Mock private BuildingRepository buildingRepository;
  @Mock private RegionRepository regionRepository;
  @Mock private CommercialAreaRepository commercialAreaRepository;
  @InjectMocks private BuildingCommandService buildingCommandService;

  @Test
  @DisplayName("create saves building with region, commercial area, and location")
  void create_savesBuildingWithLocation() {
    var region = RegionFixture.seoulGangnamYeoksamLegalDong();
    var commercialArea = CommercialAreaFixture.developedActive("Gangnam");
    given(regionRepository.findById(1L)).willReturn(Optional.of(region));
    given(commercialAreaRepository.findByIdAndStatusNot(any(), any()))
        .willReturn(Optional.of(commercialArea));
    given(buildingRepository.save(any(Building.class)))
        .willAnswer(
            invocation -> {
              Building building = invocation.getArgument(0);
              ReflectionTestUtils.setField(building, "id", 100L);
              return building;
            });

    Building result =
        buildingCommandService.create(
            new CreateBuildingCommand(
                1L,
                2L,
                "123 Teheran-ro",
                "Seed Building",
                15,
                new BigDecimal("12345.67"),
                new BigDecimal("37.5012"),
                new BigDecimal("127.0364")));

    assertThat(result.getId()).isEqualTo(100L);
    assertThat(result.getRegion()).isSameAs(region);
    assertThat(result.getCommercialArea()).isSameAs(commercialArea);
    assertThat(result.getLocation().getY()).isCloseTo(37.5012, withinDouble());
    assertThat(result.getLocation().getX()).isCloseTo(127.0364, withinDouble());
  }

  @Test
  @DisplayName("create throws NOT_FOUND_REGION when region is missing")
  void create_throwsNotFoundRegion_whenRegionMissing() {
    given(regionRepository.findById(1L)).willReturn(Optional.empty());

    assertThatThrownBy(() -> buildingCommandService.create(command()))
        .isInstanceOf(ApplicationException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.NOT_FOUND_REGION);
  }

  @Test
  @DisplayName("create throws NOT_FOUND_COMMERCIAL_AREA when commercial area is missing")
  void create_throwsNotFoundCommercialArea_whenCommercialAreaMissing() {
    given(regionRepository.findById(1L))
        .willReturn(Optional.of(RegionFixture.seoulGangnamYeoksamLegalDong()));
    given(commercialAreaRepository.findByIdAndStatusNot(any(), any())).willReturn(Optional.empty());

    assertThatThrownBy(() -> buildingCommandService.create(command()))
        .isInstanceOf(ApplicationException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.NOT_FOUND_COMMERCIAL_AREA);
  }

  @Test
  @DisplayName("create throws INVALID_PARAMETER when only one coordinate is provided")
  void create_throwsInvalidParameter_whenLocationIsIncomplete() {
    given(regionRepository.findById(1L))
        .willReturn(Optional.of(RegionFixture.seoulGangnamYeoksamLegalDong()));
    given(commercialAreaRepository.findByIdAndStatusNot(any(), any()))
        .willReturn(Optional.of(CommercialAreaFixture.developedActive("Gangnam")));

    assertThatThrownBy(
            () ->
                buildingCommandService.create(
                    new CreateBuildingCommand(
                        1L,
                        2L,
                        "123 Teheran-ro",
                        "Seed Building",
                        15,
                        null,
                        new BigDecimal("37.5012"),
                        null)))
        .isInstanceOf(ApplicationException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.INVALID_PARAMETER);
  }

  private CreateBuildingCommand command() {
    return new CreateBuildingCommand(
        1L,
        2L,
        "123 Teheran-ro",
        "Seed Building",
        15,
        new BigDecimal("12345.67"),
        new BigDecimal("37.5012"),
        new BigDecimal("127.0364"));
  }

  private org.assertj.core.data.Offset<Double> withinDouble() {
    return org.assertj.core.api.Assertions.within(1e-6);
  }
}
