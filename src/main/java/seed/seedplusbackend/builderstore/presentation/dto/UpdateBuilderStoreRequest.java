package seed.seedplusbackend.builderstore.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import seed.seedplusbackend.builderstore.application.command.UpdateBuilderStoreCommand;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreVisibilityStatus;

@Schema(description = "빌더스토어 수정 요청")
public record UpdateBuilderStoreRequest(
    @Schema(description = "지역 ID", example = "1") Long regionId,
    @Schema(description = "상권 ID", example = "1") Long commercialAreaId,
    @Schema(description = "업종 ID", example = "1") Long industryId,
    @Schema(description = "기준 건물 ID", example = "1") Long baseBuildingId,
    @Schema(description = "빌더스토어명", example = "강남 샐러드 창업안")
        @Pattern(regexp = ".*\\S.*", message = "공백만 입력할 수 없습니다.")
        @Size(max = 150)
        String name,
    @Schema(description = "입력 지표") @Valid UpdateBuilderStoreMetricsRequest metrics,
    @Schema(description = "설명", example = "테스트 빌더스토어") @Size(max = 1000) String description,
    @Schema(description = "공개 상태", example = "PRIVATE")
        BuilderStoreVisibilityStatus visibilityStatus,
    @Schema(description = "이미지 URL 목록") List<@NotBlank @Size(max = 500) String> imageUrls) {

  public UpdateBuilderStoreCommand toCommand() {
    return new UpdateBuilderStoreCommand(
        regionId,
        commercialAreaId,
        industryId,
        baseBuildingId,
        name,
        metrics == null ? null : metrics.toCommand(),
        description,
        visibilityStatus,
        imageUrls == null ? null : List.copyOf(imageUrls));
  }
}
