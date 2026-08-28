package seed.seedplusbackend.builderstore.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

@Schema(description = "저장 가상 점포 최신 데이터 반영 요청")
public record RefreshBuilderStoreBookmarkRequest(
    @Schema(description = "수집 실패 후 재시도할 실행 ID. 최초 요청에서는 생략합니다.", example = "7") @Positive
        Long collectionRunId) {}
