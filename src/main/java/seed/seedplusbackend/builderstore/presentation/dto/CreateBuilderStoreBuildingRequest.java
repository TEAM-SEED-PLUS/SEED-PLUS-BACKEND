package seed.seedplusbackend.builderstore.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import seed.seedplusbackend.building.application.command.CreateBuildingCommand;

@Schema(description = "가상 점포 생성용 건물 입력")
public record CreateBuilderStoreBuildingRequest(
    @Schema(description = "주소", example = "서울특별시 강남구 테헤란로 123") @NotBlank @Size(max = 255)
        String address,
    @Schema(description = "건물명", example = "Seed Building") @Size(max = 150) String name,
    @Schema(description = "총 층수", example = "15") @Min(0) Integer floor,
    @Schema(description = "총 면적", example = "12345.67") @DecimalMin("0.00") BigDecimal totalArea,
    @Schema(description = "위도", example = "37.5012") @DecimalMin("-90.0") @DecimalMax("90.0")
        BigDecimal latitude,
    @Schema(description = "경도", example = "127.0364") @DecimalMin("-180.0") @DecimalMax("180.0")
        BigDecimal longitude) {

  public CreateBuildingCommand toCommand(Long regionId, Long commercialAreaId) {
    return new CreateBuildingCommand(
        regionId, commercialAreaId, address, name, floor, totalArea, latitude, longitude);
  }

  @AssertTrue(message = "위도와 경도는 함께 입력해야 합니다.")
  public boolean isLocationComplete() {
    return (latitude == null && longitude == null) || (latitude != null && longitude != null);
  }
}
