package seed.seedplusbackend.builderstore.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import seed.seedplusbackend.builderstore.application.result.BuilderStoreBookmarkResult;

@Schema(description = "마이페이지 저장 가상 점포 카드")
public record BuilderStoreBookmarkResponse(
    Long bookmarkId,
    OffsetDateTime savedAt,
    BuilderStoreSummaryResponse store,
    BuilderStoreBookmarkDataResponse savedData,
    BuilderStoreBookmarkFreshnessResponse freshness,
    Long collectionRunId) {

  public static BuilderStoreBookmarkResponse from(BuilderStoreBookmarkResult result) {
    return new BuilderStoreBookmarkResponse(
        result.bookmark().getId(),
        result.bookmark().getCreatedAt(),
        BuilderStoreSummaryResponse.from(result.bookmark().getBuilderStore()),
        BuilderStoreBookmarkDataResponse.from(result.bookmark()),
        BuilderStoreBookmarkFreshnessResponse.from(result),
        result.collectionRunId());
  }
}
