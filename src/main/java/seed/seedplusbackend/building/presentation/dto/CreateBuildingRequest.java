package seed.seedplusbackend.building.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import seed.seedplusbackend.building.application.command.CreateBuildingCommand;

@Schema(description = "건물 등록 요청")
public record CreateBuildingRequest(
    @Schema(description = "지역 ID", example = "1") @NotNull Long regionId,
    @Schema(description = "상권 ID", example = "1") @NotNull Long commercialAreaId,
    @Schema(description = "주소", example = "서울특별시 강남구 테헤란로 123") @NotBlank @Size(max = 255)
        String address,
    @Schema(description = "건물명", example = "시드빌딩") @Size(max = 150) String name,
    @Schema(description = "총 층수", example = "15") @Min(0) Integer floor,
    @Schema(description = "총 면적", example = "12345.67") @DecimalMin("0.00") BigDecimal totalArea,
    @Schema(description = "위도", example = "37.5012") @DecimalMin("-90.0") @DecimalMax("90.0")
        BigDecimal latitude,
    @Schema(description = "경도", example = "127.0364") @DecimalMin("-180.0") @DecimalMax("180.0")
        BigDecimal longitude) {

  public CreateBuildingCommand toCommand() {
    return new CreateBuildingCommand(
        regionId, commercialAreaId, address, name, floor, totalArea, latitude, longitude);
  }

  @AssertTrue(message = "위도와 경도는 함께 입력해야 합니다.")
  public boolean isLocationComplete() {
    return (latitude == null && longitude == null) || (latitude != null && longitude != null);
  }
}
