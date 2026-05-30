package seed.seedplusbackend.builderstore.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreImage;

@Schema(description = "빌더스토어 이미지 응답")
public record BuilderStoreImageResponse(
    @Schema(description = "이미지 ID", example = "1") Long imageId,
    @Schema(description = "이미지 URL", example = "https://example.com/image.png") String imageUrl,
    @Schema(description = "노출 순서", example = "0") int displayOrder) {

  public static BuilderStoreImageResponse from(BuilderStoreImage image) {
    return new BuilderStoreImageResponse(
        image.getId(), image.getImageUrl(), image.getDisplayOrder());
  }
}
