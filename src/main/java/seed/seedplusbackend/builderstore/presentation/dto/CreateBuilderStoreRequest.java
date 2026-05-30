package seed.seedplusbackend.builderstore.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;
import seed.seedplusbackend.builderstore.application.command.CreateBuilderStoreCommand;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreVisibilityStatus;

@Schema(description = "가상 점포 생성 요청")
public record CreateBuilderStoreRequest(
    @Schema(description = "지역 ID", example = "1") @NotNull @Positive Long regionId,
    @Schema(description = "상권 ID", example = "1") @NotNull @Positive Long commercialAreaId,
    @Schema(description = "업종 ID", example = "1") @NotNull @Positive Long industryId,
    @Schema(description = "건물 입력") @Valid @NotNull CreateBuilderStoreBuildingRequest building,
    @Schema(description = "가상 점포명", example = "강남 샐러드 창업안") @NotBlank @Size(max = 150) String name,
    @Schema(description = "입력 지표") @Valid @NotNull CreateBuilderStoreMetricsRequest metrics,
    @Schema(description = "설명", example = "테스트 가상 점포") @Size(max = 1000) String description,
    @Schema(description = "공개 상태", example = "PUBLIC", defaultValue = "PUBLIC")
        BuilderStoreVisibilityStatus visibilityStatus,
    @Schema(description = "이미지 URL 목록") List<@NotBlank @Size(max = 500) String> imageUrls) {

  public CreateBuilderStoreCommand toCommand() {
    return new CreateBuilderStoreCommand(
        regionId,
        commercialAreaId,
        industryId,
        building.toCommand(regionId, commercialAreaId),
        name,
        metrics.toCommand(),
        description,
        visibilityStatus,
        imageUrls == null ? List.of() : List.copyOf(imageUrls));
  }
}
