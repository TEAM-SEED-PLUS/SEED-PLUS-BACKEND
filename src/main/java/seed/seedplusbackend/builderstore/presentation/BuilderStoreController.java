package seed.seedplusbackend.builderstore.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seed.seedplusbackend.builderstore.application.BuilderStoreCommandService;
import seed.seedplusbackend.builderstore.application.BuilderStoreQueryService;
import seed.seedplusbackend.builderstore.presentation.dto.BuilderStoreCommentPageRequest;
import seed.seedplusbackend.builderstore.presentation.dto.BuilderStoreCommentResponse;
import seed.seedplusbackend.builderstore.presentation.dto.BuilderStoreDetailResponse;
import seed.seedplusbackend.builderstore.presentation.dto.BuilderStoreSearchRequest;
import seed.seedplusbackend.builderstore.presentation.dto.BuilderStoreSummaryResponse;
import seed.seedplusbackend.builderstore.presentation.dto.CreateBuilderStoreCommentRequest;
import seed.seedplusbackend.builderstore.presentation.dto.CreateBuilderStoreRequest;
import seed.seedplusbackend.builderstore.presentation.dto.UpdateBuilderStoreCommentRequest;
import seed.seedplusbackend.builderstore.presentation.dto.UpdateBuilderStoreRequest;
import seed.seedplusbackend.global.response.ApiResponse;
import seed.seedplusbackend.global.response.PageResponse;
import seed.seedplusbackend.global.security.AuthenticatedUser;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/builder-stores")
public class BuilderStoreController implements BuilderStoreApi {

  private final BuilderStoreQueryService builderStoreQueryService;
  private final BuilderStoreCommandService builderStoreCommandService;

  @Override
  public ResponseEntity<ApiResponse<PageResponse<BuilderStoreSummaryResponse>>> getBuilderStores(
      @Valid @ParameterObject BuilderStoreSearchRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(
            PageResponse.from(
                builderStoreQueryService.searchPublic(request.toQuery()),
                BuilderStoreSummaryResponse::from)));
  }

  @Override
  public ResponseEntity<ApiResponse<BuilderStoreDetailResponse>> getBuilderStore(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @PathVariable Long builderStoreId) {
    Long userId = authenticatedUser == null ? null : authenticatedUser.getId();
    return ResponseEntity.ok(
        ApiResponse.success(
            BuilderStoreDetailResponse.from(
                builderStoreQueryService.getPublicBuilderStore(builderStoreId, userId))));
  }

  @Override
  public ResponseEntity<ApiResponse<BuilderStoreDetailResponse>> createBuilderStore(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @Valid @RequestBody CreateBuilderStoreRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            ApiResponse.success(
                HttpStatus.CREATED,
                BuilderStoreDetailResponse.from(
                    builderStoreCommandService.create(
                        authenticatedUser.getId(), request.toCommand()))));
  }

  @Override
  public ResponseEntity<ApiResponse<BuilderStoreDetailResponse>> updateBuilderStore(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @PathVariable Long builderStoreId,
      @Valid @RequestBody UpdateBuilderStoreRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(
            BuilderStoreDetailResponse.from(
                builderStoreCommandService.update(
                    authenticatedUser.getId(), builderStoreId, request.toCommand()))));
  }

  @Override
  public ResponseEntity<ApiResponse<Void>> deleteBuilderStore(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @PathVariable Long builderStoreId) {
    builderStoreCommandService.delete(authenticatedUser.getId(), builderStoreId);
    return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK));
  }

  @Override
  public ResponseEntity<ApiResponse<Void>> likeBuilderStore(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @PathVariable Long builderStoreId) {
    builderStoreCommandService.like(authenticatedUser.getId(), builderStoreId);
    return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK));
  }

  @Override
  public ResponseEntity<ApiResponse<Void>> unlikeBuilderStore(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @PathVariable Long builderStoreId) {
    builderStoreCommandService.unlike(authenticatedUser.getId(), builderStoreId);
    return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK));
  }

  @Override
  public ResponseEntity<ApiResponse<Void>> bookmarkBuilderStore(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @PathVariable Long builderStoreId) {
    builderStoreCommandService.bookmark(authenticatedUser.getId(), builderStoreId);
    return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK));
  }

  @Override
  public ResponseEntity<ApiResponse<Void>> unbookmarkBuilderStore(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @PathVariable Long builderStoreId) {
    builderStoreCommandService.unbookmark(authenticatedUser.getId(), builderStoreId);
    return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK));
  }

  @Override
  public ResponseEntity<ApiResponse<PageResponse<BuilderStoreCommentResponse>>> getComments(
      @PathVariable Long builderStoreId,
      @Valid @ParameterObject BuilderStoreCommentPageRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(
            PageResponse.from(
                builderStoreQueryService.getComments(builderStoreId, request.toQuery()),
                BuilderStoreCommentResponse::from)));
  }

  @Override
  public ResponseEntity<ApiResponse<BuilderStoreCommentResponse>> createComment(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @PathVariable Long builderStoreId,
      @Valid @RequestBody CreateBuilderStoreCommentRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            ApiResponse.success(
                HttpStatus.CREATED,
                BuilderStoreCommentResponse.from(
                    builderStoreCommandService.createComment(
                        authenticatedUser.getId(), builderStoreId, request.toCommand()))));
  }

  @Override
  public ResponseEntity<ApiResponse<BuilderStoreCommentResponse>> updateComment(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @PathVariable Long builderStoreId,
      @PathVariable Long commentId,
      @Valid @RequestBody UpdateBuilderStoreCommentRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(
            BuilderStoreCommentResponse.from(
                builderStoreCommandService.updateComment(
                    authenticatedUser.getId(), builderStoreId, commentId, request.toCommand()))));
  }

  @Override
  public ResponseEntity<ApiResponse<Void>> deleteComment(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @PathVariable Long builderStoreId,
      @PathVariable Long commentId) {
    builderStoreCommandService.deleteComment(authenticatedUser.getId(), builderStoreId, commentId);
    return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK));
  }
}
