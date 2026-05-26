package seed.seedplusbackend.builderstore.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seed.seedplusbackend.builderstore.application.query.BuilderStoreCommentPageQuery;
import seed.seedplusbackend.builderstore.application.query.BuilderStorePageQuery;
import seed.seedplusbackend.builderstore.application.query.BuilderStoreSearchQuery;
import seed.seedplusbackend.builderstore.application.result.BuilderStoreCommentResult;
import seed.seedplusbackend.builderstore.application.result.BuilderStoreDetailResult;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStore;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreComment;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreImage;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreVisibilityStatus;
import seed.seedplusbackend.builderstore.domain.repository.BuilderStoreBookmarkRepository;
import seed.seedplusbackend.builderstore.domain.repository.BuilderStoreCommentRepository;
import seed.seedplusbackend.builderstore.domain.repository.BuilderStoreImageRepository;
import seed.seedplusbackend.builderstore.domain.repository.BuilderStoreLikeRepository;
import seed.seedplusbackend.builderstore.domain.repository.BuilderStoreRepository;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.score.domain.entity.ScoreSnapshot;
import seed.seedplusbackend.score.domain.entity.ScoreTargetType;
import seed.seedplusbackend.score.domain.entity.ScoreType;
import seed.seedplusbackend.score.domain.repository.ScoreSnapshotRepository;
import seed.seedplusbackend.user.domain.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class BuilderStoreQueryService {

  private final BuilderStoreRepository builderStoreRepository;
  private final BuilderStoreImageRepository builderStoreImageRepository;
  private final BuilderStoreLikeRepository builderStoreLikeRepository;
  private final BuilderStoreBookmarkRepository builderStoreBookmarkRepository;
  private final BuilderStoreCommentRepository builderStoreCommentRepository;
  private final ScoreSnapshotRepository scoreSnapshotRepository;
  private final UserRepository userRepository;

  @Transactional(readOnly = true)
  public Page<BuilderStore> searchPublic(BuilderStoreSearchQuery query) {
    validateAreaRange(query.minArea(), query.maxArea());

    return builderStoreRepository.searchPublic(
        query.regionId(),
        query.commercialAreaId(),
        query.industryId(),
        query.minArea(),
        query.maxArea(),
        query.pageable());
  }

  @Transactional(readOnly = true)
  public BuilderStoreDetailResult getPublicBuilderStore(Long builderStoreId, Long userId) {
    BuilderStore builderStore =
        builderStoreRepository
            .findByIdAndVisibilityStatus(builderStoreId, BuilderStoreVisibilityStatus.PUBLIC)
            .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND_BUILDER_STORE));

    return toDetailResult(builderStore, userId);
  }

  @Transactional(readOnly = true)
  public Page<BuilderStore> getMyBuilderStores(Long userId, BuilderStorePageQuery query) {
    validateUserExists(userId);

    return builderStoreRepository.findByUser_IdAndVisibilityStatusNot(
        userId, BuilderStoreVisibilityStatus.DELETED, query.pageable());
  }

  @Transactional(readOnly = true)
  public BuilderStoreDetailResult getMyBuilderStore(Long userId, Long builderStoreId) {
    validateUserExists(userId);
    BuilderStore builderStore = getNotDeletedBuilderStore(builderStoreId);
    validateOwner(builderStore, userId);

    return toDetailResult(builderStore, userId);
  }

  @Transactional(readOnly = true)
  public Page<BuilderStoreCommentResult> getComments(
      Long builderStoreId, BuilderStoreCommentPageQuery query) {
    builderStoreRepository
        .findByIdAndVisibilityStatus(builderStoreId, BuilderStoreVisibilityStatus.PUBLIC)
        .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND_BUILDER_STORE));

    Page<BuilderStoreComment> comments =
        builderStoreCommentRepository.findByBuilderStore_IdAndParentIsNullOrderByCreatedAtAscIdAsc(
            builderStoreId, query.pageable());

    List<BuilderStoreCommentResult> results =
        comments.getContent().stream()
            .map(
                comment ->
                    new BuilderStoreCommentResult(
                        comment,
                        builderStoreCommentRepository.findByParent_IdOrderByCreatedAtAscIdAsc(
                            comment.getId())))
            .toList();

    return new PageImpl<>(results, query.pageable(), comments.getTotalElements());
  }

  private BuilderStoreDetailResult toDetailResult(BuilderStore builderStore, Long userId) {
    Long builderStoreId = builderStore.getId();
    List<BuilderStoreImage> images =
        builderStoreImageRepository.findByBuilderStore_IdOrderByDisplayOrderAscIdAsc(
            builderStoreId);
    ScoreSnapshot latestScore =
        scoreSnapshotRepository
            .findFirstByBuilderStoreIdAndTargetTypeAndScoreTypeOrderByReferenceMonthDescIdDesc(
                builderStoreId, ScoreTargetType.BUILDER_STORE, ScoreType.PROPERTY)
            .orElse(null);

    boolean liked =
        userId != null
            && builderStoreLikeRepository.existsByBuilderStore_IdAndUser_Id(builderStoreId, userId);
    boolean bookmarked =
        userId != null
            && builderStoreBookmarkRepository.existsByBuilderStore_IdAndUser_Id(
                builderStoreId, userId);

    return new BuilderStoreDetailResult(builderStore, images, latestScore, liked, bookmarked);
  }

  private BuilderStore getNotDeletedBuilderStore(Long builderStoreId) {
    return builderStoreRepository
        .findByIdAndVisibilityStatusNot(builderStoreId, BuilderStoreVisibilityStatus.DELETED)
        .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND_BUILDER_STORE));
  }

  private void validateUserExists(Long userId) {
    if (!userRepository.existsById(userId)) {
      throw new ApplicationException(ErrorCode.NOT_FOUND_USER);
    }
  }

  private void validateOwner(BuilderStore builderStore, Long userId) {
    if (!builderStore.isOwnedBy(userId)) {
      throw new ApplicationException(ErrorCode.NOT_OWNER_BUILDER_STORE);
    }
  }

  private void validateAreaRange(Integer minArea, Integer maxArea) {
    if (minArea != null && maxArea != null && minArea > maxArea) {
      throw new ApplicationException(ErrorCode.INVALID_PARAMETER);
    }
  }
}
