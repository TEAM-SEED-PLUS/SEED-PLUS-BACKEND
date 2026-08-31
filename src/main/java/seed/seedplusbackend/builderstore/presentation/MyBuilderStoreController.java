package seed.seedplusbackend.builderstore.presentation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seed.seedplusbackend.builderstore.application.BuilderStoreBookmarkService;
import seed.seedplusbackend.builderstore.application.BuilderStoreQueryService;
import seed.seedplusbackend.builderstore.presentation.dto.BuilderStoreBookmarkPageRequest;
import seed.seedplusbackend.builderstore.presentation.dto.BuilderStoreBookmarkResponse;
import seed.seedplusbackend.builderstore.presentation.dto.BuilderStoreDetailResponse;
import seed.seedplusbackend.builderstore.presentation.dto.BuilderStorePageRequest;
import seed.seedplusbackend.builderstore.presentation.dto.BuilderStoreSummaryResponse;
import seed.seedplusbackend.builderstore.presentation.dto.RefreshBuilderStoreBookmarkRequest;
import seed.seedplusbackend.global.response.ApiResponse;
import seed.seedplusbackend.global.response.PageResponse;
import seed.seedplusbackend.global.security.AuthenticatedUser;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/me/builder-stores")
public class MyBuilderStoreController implements MyBuilderStoreApi {

  private final BuilderStoreQueryService builderStoreQueryService;
  private final BuilderStoreBookmarkService builderStoreBookmarkService;

  @Override
  public ResponseEntity<ApiResponse<PageResponse<BuilderStoreSummaryResponse>>> getMyBuilderStores(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @Valid @ParameterObject BuilderStorePageRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(
            PageResponse.from(
                builderStoreQueryService.getMyBuilderStores(
                    authenticatedUser.getId(), request.toQuery()),
                BuilderStoreSummaryResponse::from)));
  }

  @Override
  public ResponseEntity<ApiResponse<BuilderStoreDetailResponse>> getMyBuilderStore(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @PathVariable @Positive Long builderStoreId) {
    return ResponseEntity.ok(
        ApiResponse.success(
            BuilderStoreDetailResponse.from(
                builderStoreQueryService.getMyBuilderStore(
                    authenticatedUser.getId(), builderStoreId))));
  }

  @Override
  public ResponseEntity<ApiResponse<PageResponse<BuilderStoreBookmarkResponse>>> getBookmarks(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @Valid @ParameterObject BuilderStoreBookmarkPageRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(
            PageResponse.from(
                builderStoreBookmarkService.getBookmarks(
                    authenticatedUser.getId(), request.resolvedPage(), request.resolvedSize()),
                BuilderStoreBookmarkResponse::from)));
  }

  @Override
  public ResponseEntity<ApiResponse<BuilderStoreBookmarkResponse>> refreshBookmark(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @PathVariable @Positive Long bookmarkId,
      @Valid @RequestBody(required = false) RefreshBuilderStoreBookmarkRequest request) {
    Long collectionRunId = request == null ? null : request.collectionRunId();
    return ResponseEntity.ok(
        ApiResponse.success(
            BuilderStoreBookmarkResponse.from(
                builderStoreBookmarkService.refresh(
                    authenticatedUser.getId(), bookmarkId, collectionRunId))));
  }
}
