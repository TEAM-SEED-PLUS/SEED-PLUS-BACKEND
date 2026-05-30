package seed.seedplusbackend.builderstore.application;

import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seed.seedplusbackend.builderstore.application.command.BuilderStoreMetricsCommand;
import seed.seedplusbackend.builderstore.application.command.CreateBuilderStoreCommand;
import seed.seedplusbackend.builderstore.application.command.CreateBuilderStoreCommentCommand;
import seed.seedplusbackend.builderstore.application.command.UpdateBuilderStoreCommand;
import seed.seedplusbackend.builderstore.application.command.UpdateBuilderStoreCommentCommand;
import seed.seedplusbackend.builderstore.application.event.BuilderStoreCreatedEvent;
import seed.seedplusbackend.builderstore.application.result.BuilderStoreCommentResult;
import seed.seedplusbackend.builderstore.application.result.BuilderStoreDetailResult;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStore;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreBookmark;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreComment;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreImage;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreLike;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreVisibilityStatus;
import seed.seedplusbackend.builderstore.domain.repository.BuilderStoreBookmarkRepository;
import seed.seedplusbackend.builderstore.domain.repository.BuilderStoreCommentRepository;
import seed.seedplusbackend.builderstore.domain.repository.BuilderStoreImageRepository;
import seed.seedplusbackend.builderstore.domain.repository.BuilderStoreLikeRepository;
import seed.seedplusbackend.builderstore.domain.repository.BuilderStoreRepository;
import seed.seedplusbackend.building.application.BuildingCommandService;
import seed.seedplusbackend.building.domain.entity.Building;
import seed.seedplusbackend.building.domain.repository.BuildingRepository;
import seed.seedplusbackend.commercial.domain.entity.CommercialArea;
import seed.seedplusbackend.commercial.domain.entity.CommercialAreaStatus;
import seed.seedplusbackend.commercial.domain.repository.CommercialAreaRepository;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.industry.domain.entity.Industry;
import seed.seedplusbackend.industry.domain.entity.IndustryStatus;
import seed.seedplusbackend.industry.domain.repository.IndustryRepository;
import seed.seedplusbackend.region.domain.entity.Region;
import seed.seedplusbackend.region.domain.repository.RegionRepository;
import seed.seedplusbackend.score.domain.entity.ScoreSnapshot;
import seed.seedplusbackend.score.domain.entity.ScoreTargetType;
import seed.seedplusbackend.score.domain.entity.ScoreType;
import seed.seedplusbackend.score.domain.repository.ScoreSnapshotRepository;
import seed.seedplusbackend.user.domain.entity.User;
import seed.seedplusbackend.user.domain.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class BuilderStoreCommandService {

  private final BuilderStoreRepository builderStoreRepository;
  private final BuilderStoreImageRepository builderStoreImageRepository;
  private final BuilderStoreLikeRepository builderStoreLikeRepository;
  private final BuilderStoreBookmarkRepository builderStoreBookmarkRepository;
  private final BuilderStoreCommentRepository builderStoreCommentRepository;
  private final UserRepository userRepository;
  private final RegionRepository regionRepository;
  private final CommercialAreaRepository commercialAreaRepository;
  private final IndustryRepository industryRepository;
  private final BuildingCommandService buildingCommandService;
  private final BuildingRepository buildingRepository;
  private final ScoreSnapshotRepository scoreSnapshotRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public BuilderStoreDetailResult create(Long userId, CreateBuilderStoreCommand command) {
    BuilderStoreVisibilityStatus visibilityStatus = resolveCreateStatus(command.visibilityStatus());
    BuilderStoreMetricsCommand metrics = command.metrics();
    User user = getUser(userId);
    Region region = getRegion(command.regionId());
    CommercialArea commercialArea = getCommercialArea(command.commercialAreaId());
    Industry industry = getIndustry(command.industryId());
    Building baseBuilding =
        buildingCommandService.resolveOrCreate(command.building(), region, commercialArea);

    BuilderStore builderStore =
        builderStoreRepository.save(
            BuilderStore.builder()
                .user(user)
                .region(region)
                .commercialArea(commercialArea)
                .industry(industry)
                .baseBuilding(baseBuilding)
                .name(command.name())
                .area(metrics.area())
                .expectedMonthlySales(metrics.expectedMonthlySales())
                .expectedProfitRate(metrics.expectedProfitRate())
                .investmentPaybackMonths(metrics.investmentPaybackMonths())
                .propertyScore(0)
                .monthlyRent(metrics.monthlyRent())
                .deposit(metrics.deposit())
                .investmentAmount(metrics.investmentAmount())
                .description(command.description())
                .likeCount(0L)
                .commentCount(0L)
                .visibilityStatus(visibilityStatus)
                .uploadedAt(OffsetDateTime.now())
                .build());

    replaceImages(builderStore, command.imageUrls());
    eventPublisher.publishEvent(new BuilderStoreCreatedEvent(builderStore.getId()));
    return toDetailResult(builderStore, userId);
  }

  @Transactional
  public BuilderStoreDetailResult update(
      Long userId, Long builderStoreId, UpdateBuilderStoreCommand command) {
    BuilderStore builderStore = getNotDeletedBuilderStore(builderStoreId);
    validateOwner(builderStore, userId);

    BuilderStoreVisibilityStatus visibilityStatus =
        resolveUpdateStatus(command.visibilityStatus(), builderStore.getVisibilityStatus());
    BuilderStoreMetricsCommand metrics = command.metrics();

    builderStore.update(
        command.regionId() == null ? builderStore.getRegion() : getRegion(command.regionId()),
        command.commercialAreaId() == null
            ? builderStore.getCommercialArea()
            : getCommercialArea(command.commercialAreaId()),
        command.industryId() == null
            ? builderStore.getIndustry()
            : getIndustry(command.industryId()),
        command.baseBuildingId() == null
            ? builderStore.getBaseBuilding()
            : getBaseBuilding(command.baseBuildingId()),
        command.name() == null ? builderStore.getName() : command.name(),
        metrics == null || metrics.area() == null ? builderStore.getArea() : metrics.area(),
        metrics == null || metrics.expectedMonthlySales() == null
            ? builderStore.getExpectedMonthlySales()
            : metrics.expectedMonthlySales(),
        metrics == null || metrics.expectedProfitRate() == null
            ? builderStore.getExpectedProfitRate()
            : metrics.expectedProfitRate(),
        metrics == null || metrics.investmentPaybackMonths() == null
            ? builderStore.getInvestmentPaybackMonths()
            : metrics.investmentPaybackMonths(),
        metrics == null || metrics.monthlyRent() == null
            ? builderStore.getMonthlyRent()
            : metrics.monthlyRent(),
        metrics == null || metrics.deposit() == null
            ? builderStore.getDeposit()
            : metrics.deposit(),
        metrics == null || metrics.investmentAmount() == null
            ? builderStore.getInvestmentAmount()
            : metrics.investmentAmount(),
        command.description() == null ? builderStore.getDescription() : command.description(),
        visibilityStatus);

    if (command.imageUrls() != null) {
      replaceImages(builderStore, command.imageUrls());
    }

    return toDetailResult(builderStore, userId);
  }

  @Transactional
  public void delete(Long userId, Long builderStoreId) {
    BuilderStore builderStore = getNotDeletedBuilderStore(builderStoreId);
    validateOwner(builderStore, userId);

    builderStore.delete();
  }

  @Transactional
  public void like(Long userId, Long builderStoreId) {
    User user = getUser(userId);
    BuilderStore builderStore = getPublicBuilderStore(builderStoreId);
    if (builderStoreLikeRepository.existsByBuilderStore_IdAndUser_Id(builderStoreId, userId)) {
      throw new ApplicationException(ErrorCode.ALREADY_LIKED);
    }

    builderStoreLikeRepository.save(
        BuilderStoreLike.builder().builderStore(builderStore).user(user).build());
    builderStore.increaseLikeCount();
  }

  @Transactional
  public void unlike(Long userId, Long builderStoreId) {
    BuilderStore builderStore = getNotDeletedBuilderStore(builderStoreId);
    BuilderStoreLike like =
        builderStoreLikeRepository
            .findByBuilderStore_IdAndUser_Id(builderStoreId, userId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_LIKED));

    builderStoreLikeRepository.delete(like);
    builderStore.decreaseLikeCount();
  }

  @Transactional
  public void bookmark(Long userId, Long builderStoreId) {
    User user = getUser(userId);
    BuilderStore builderStore = getPublicBuilderStore(builderStoreId);
    if (builderStoreBookmarkRepository.existsByBuilderStore_IdAndUser_Id(builderStoreId, userId)) {
      throw new ApplicationException(ErrorCode.ALREADY_BOOKMARKED);
    }

    builderStoreBookmarkRepository.save(
        BuilderStoreBookmark.builder().builderStore(builderStore).user(user).build());
  }

  @Transactional
  public void unbookmark(Long userId, Long builderStoreId) {
    getNotDeletedBuilderStore(builderStoreId);
    BuilderStoreBookmark bookmark =
        builderStoreBookmarkRepository
            .findByBuilderStore_IdAndUser_Id(builderStoreId, userId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_BOOKMARKED));

    builderStoreBookmarkRepository.delete(bookmark);
  }

  @Transactional
  public BuilderStoreCommentResult createComment(
      Long userId, Long builderStoreId, CreateBuilderStoreCommentCommand command) {
    User user = getUser(userId);
    BuilderStore builderStore = getPublicBuilderStore(builderStoreId);
    BuilderStoreComment parent = getValidParentComment(builderStoreId, command.parentCommentId());

    BuilderStoreComment comment =
        builderStoreCommentRepository.save(
            BuilderStoreComment.builder()
                .builderStore(builderStore)
                .parent(parent)
                .user(user)
                .content(command.content())
                .build());
    builderStore.increaseCommentCount();

    return new BuilderStoreCommentResult(comment, List.of());
  }

  @Transactional
  public BuilderStoreCommentResult updateComment(
      Long userId, Long builderStoreId, Long commentId, UpdateBuilderStoreCommentCommand command) {
    getNotDeletedBuilderStore(builderStoreId);
    BuilderStoreComment comment = getComment(builderStoreId, commentId);
    validateCommentOwner(comment, userId);

    comment.updateContent(command.content());
    return toCommentResult(comment);
  }

  @Transactional
  public void deleteComment(Long userId, Long builderStoreId, Long commentId) {
    BuilderStore builderStore = getNotDeletedBuilderStore(builderStoreId);
    BuilderStoreComment comment = getComment(builderStoreId, commentId);
    validateCommentOwner(comment, userId);

    long deletedCount = 1;
    if (!comment.isReply()) {
      List<BuilderStoreComment> replies =
          builderStoreCommentRepository.findByParent_IdOrderByCreatedAtAscIdAsc(comment.getId());
      for (BuilderStoreComment reply : replies) {
        builderStoreCommentRepository.delete(reply);
      }
      deletedCount += replies.size();
    }

    builderStoreCommentRepository.delete(comment);
    builderStore.decreaseCommentCount(deletedCount);
  }

  private void replaceImages(BuilderStore builderStore, List<String> imageUrls) {
    builderStoreImageRepository.deleteByBuilderStore_Id(builderStore.getId());
    builderStoreImageRepository.flush();

    List<String> resolvedImageUrls = imageUrls == null ? List.of() : imageUrls;
    for (int i = 0; i < resolvedImageUrls.size(); i++) {
      builderStoreImageRepository.save(
          BuilderStoreImage.builder()
              .builderStore(builderStore)
              .imageUrl(resolvedImageUrls.get(i))
              .displayOrder(i)
              .build());
    }
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
        builderStoreLikeRepository.existsByBuilderStore_IdAndUser_Id(builderStoreId, userId);
    boolean bookmarked =
        builderStoreBookmarkRepository.existsByBuilderStore_IdAndUser_Id(builderStoreId, userId);

    return new BuilderStoreDetailResult(builderStore, images, latestScore, liked, bookmarked);
  }

  private BuilderStoreCommentResult toCommentResult(BuilderStoreComment comment) {
    List<BuilderStoreComment> replies =
        comment.isReply()
            ? List.of()
            : builderStoreCommentRepository.findByParent_IdOrderByCreatedAtAscIdAsc(
                comment.getId());
    return new BuilderStoreCommentResult(comment, replies);
  }

  private User getUser(Long userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND_USER));
  }

  private Region getRegion(Long regionId) {
    return regionRepository
        .findById(regionId)
        .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND_REGION));
  }

  private CommercialArea getCommercialArea(Long commercialAreaId) {
    return commercialAreaRepository
        .findByIdAndStatusNot(commercialAreaId, CommercialAreaStatus.DELETED)
        .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND_COMMERCIAL_AREA));
  }

  private Industry getIndustry(Long industryId) {
    return industryRepository
        .findByIdAndStatus(industryId, IndustryStatus.ACTIVE)
        .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND_INDUSTRY));
  }

  private Building getBaseBuilding(Long buildingId) {
    if (buildingId == null) {
      return null;
    }

    return buildingRepository
        .findById(buildingId)
        .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND_BUILDING));
  }

  private BuilderStore getPublicBuilderStore(Long builderStoreId) {
    return builderStoreRepository
        .findByIdAndVisibilityStatus(builderStoreId, BuilderStoreVisibilityStatus.PUBLIC)
        .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND_BUILDER_STORE));
  }

  private BuilderStore getNotDeletedBuilderStore(Long builderStoreId) {
    return builderStoreRepository
        .findByIdAndVisibilityStatusNot(builderStoreId, BuilderStoreVisibilityStatus.DELETED)
        .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND_BUILDER_STORE));
  }

  private BuilderStoreComment getComment(Long builderStoreId, Long commentId) {
    return builderStoreCommentRepository
        .findByIdAndBuilderStore_Id(commentId, builderStoreId)
        .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND_COMMENT));
  }

  private BuilderStoreComment getValidParentComment(Long builderStoreId, Long parentCommentId) {
    if (parentCommentId == null) {
      return null;
    }

    BuilderStoreComment parent =
        builderStoreCommentRepository
            .findByIdAndBuilderStore_Id(parentCommentId, builderStoreId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.INVALID_PARENT_COMMENT));
    if (parent.isReply()) {
      throw new ApplicationException(ErrorCode.INVALID_PARENT_COMMENT);
    }
    return parent;
  }

  private BuilderStoreVisibilityStatus resolveCreateStatus(
      BuilderStoreVisibilityStatus visibilityStatus) {
    BuilderStoreVisibilityStatus resolved =
        visibilityStatus == null ? BuilderStoreVisibilityStatus.PUBLIC : visibilityStatus;
    if (resolved == BuilderStoreVisibilityStatus.DELETED) {
      throw new ApplicationException(ErrorCode.INVALID_BUILDER_STORE_STATUS);
    }
    return resolved;
  }

  private BuilderStoreVisibilityStatus resolveUpdateStatus(
      BuilderStoreVisibilityStatus requested, BuilderStoreVisibilityStatus current) {
    if (requested == null) {
      return current;
    }
    if (requested == BuilderStoreVisibilityStatus.DELETED) {
      throw new ApplicationException(ErrorCode.INVALID_BUILDER_STORE_STATUS);
    }
    return requested;
  }

  private void validateOwner(BuilderStore builderStore, Long userId) {
    if (!builderStore.isOwnedBy(userId)) {
      throw new ApplicationException(ErrorCode.NOT_OWNER_BUILDER_STORE);
    }
  }

  private void validateCommentOwner(BuilderStoreComment comment, Long userId) {
    if (!comment.isOwnedBy(userId)) {
      throw new ApplicationException(ErrorCode.NOT_OWNER_COMMENT);
    }
  }
}
