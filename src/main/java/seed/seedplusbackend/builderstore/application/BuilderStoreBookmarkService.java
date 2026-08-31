package seed.seedplusbackend.builderstore.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import seed.seedplusbackend.analysis.application.AnalysisDataCollectionCoordinator;
import seed.seedplusbackend.analysis.application.result.AnalysisDataCollectionResult;
import seed.seedplusbackend.analysis.domain.entity.AnalysisCollectionRunStatus;
import seed.seedplusbackend.analysis.domain.entity.AnalysisCollectionType;
import seed.seedplusbackend.builderstore.application.port.BuilderStoreBookmarkSnapshotResolver;
import seed.seedplusbackend.builderstore.application.result.BuilderStoreBookmarkResult;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStore;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreBookmark;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreBookmarkSnapshot;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreVisibilityStatus;
import seed.seedplusbackend.builderstore.domain.repository.BuilderStoreBookmarkRepository;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.user.domain.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class BuilderStoreBookmarkService {

  private final BuilderStoreBookmarkRepository bookmarkRepository;
  private final BuilderStoreBookmarkSnapshotResolver snapshotResolver;
  private final AnalysisDataCollectionCoordinator collectionCoordinator;
  private final UserRepository userRepository;

  public Page<BuilderStoreBookmarkResult> getBookmarks(Long userId, int page, int size) {
    validateUser(userId);
    return bookmarkRepository
        .findByUser_IdAndBuilderStore_VisibilityStatusOrderByCreatedAtDesc(
            userId, BuilderStoreVisibilityStatus.PUBLIC, PageRequest.of(page, size))
        .map(bookmark -> BuilderStoreBookmarkResult.of(bookmark, resolveCurrent(bookmark), null));
  }

  public BuilderStoreBookmarkResult refresh(Long userId, Long bookmarkId, Long collectionRunId) {
    BuilderStoreBookmark bookmark =
        bookmarkRepository
            .findByIdAndUser_IdAndBuilderStore_VisibilityStatus(
                bookmarkId, userId, BuilderStoreVisibilityStatus.PUBLIC)
            .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_BOOKMARKED));
    BuilderStore builderStore = bookmark.getBuilderStore();
    String regionCode = builderStore.getRegion().getCode();
    String industryCode = builderStore.getIndustry().getIndustryCode();

    AnalysisDataCollectionResult collectionResult =
        collectionRunId == null
            ? collectionCoordinator.collectWithoutRealtime(
                userId, AnalysisCollectionType.PROFIT, regionCode, industryCode)
            : collectionCoordinator.retryWithoutRealtime(
                userId, collectionRunId, AnalysisCollectionType.PROFIT, regionCode, industryCode);
    if (collectionResult.status() != AnalysisCollectionRunStatus.COMPLETED) {
      throw new ApplicationException(
          ErrorCode.ANALYSIS_DATA_COLLECTION_FAILED,
          "runId=%s, failedDataTypes=%s"
              .formatted(
                  collectionResult.runId(), String.join(",", collectionResult.failedDataTypes())));
    }

    BuilderStoreBookmarkSnapshot latest = resolveCurrent(bookmark);
    bookmark.applySnapshot(latest);
    bookmarkRepository.save(bookmark);
    return BuilderStoreBookmarkResult.of(bookmark, latest, collectionResult.runId());
  }

  private BuilderStoreBookmarkSnapshot resolveCurrent(BuilderStoreBookmark bookmark) {
    BuilderStore builderStore = bookmark.getBuilderStore();
    return snapshotResolver.resolve(
        builderStore.getRegion().getCode(),
        builderStore.getIndustry().getIndustryCode(),
        builderStore.getRegion().getId(),
        builderStore.getCommercialArea().getId());
  }

  private void validateUser(Long userId) {
    if (!userRepository.existsById(userId)) {
      throw new ApplicationException(ErrorCode.NOT_FOUND_USER);
    }
  }
}
