package seed.seedplusbackend.builderstore.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Schema(description = "저장 가상 점포 목록 요청")
public record BuilderStoreBookmarkPageRequest(
    @Schema(description = "페이지 번호 (0부터 시작)", example = "0") @Min(0) Integer page,
    @Schema(description = "페이지 크기", example = "20") @Min(1) @Max(100) Integer size) {

  public int resolvedPage() {
    return page == null ? 0 : page;
  }

  public int resolvedSize() {
    return size == null ? 20 : size;
  }
}
