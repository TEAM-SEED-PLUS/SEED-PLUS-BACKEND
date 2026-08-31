package seed.seedplusbackend.builderstore.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import seed.seedplusbackend.builderstore.presentation.dto.BuilderStoreBookmarkPageRequest;
import seed.seedplusbackend.builderstore.presentation.dto.BuilderStoreBookmarkResponse;
import seed.seedplusbackend.builderstore.presentation.dto.BuilderStoreDetailResponse;
import seed.seedplusbackend.builderstore.presentation.dto.BuilderStorePageRequest;
import seed.seedplusbackend.builderstore.presentation.dto.BuilderStoreSummaryResponse;
import seed.seedplusbackend.builderstore.presentation.dto.RefreshBuilderStoreBookmarkRequest;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.global.response.ApiResponse;
import seed.seedplusbackend.global.response.PageResponse;
import seed.seedplusbackend.global.security.AuthenticatedUser;
import seed.seedplusbackend.global.swagger.annotation.ApiErrorCodeExamples;

@Tag(name = "내 가상 점포", description = "내 가상 점포 API")
public interface MyBuilderStoreApi {

  @Operation(
      summary = "내 가상 점포 목록 조회",
      operationId = "getMyBuilderStores",
      security = @SecurityRequirement(name = "bearerAuth"))
  @ApiErrorCodeExamples({ErrorCode.UNAUTHORIZED, ErrorCode.NOT_FOUND_USER, ErrorCode.INVALID_SORT})
  @GetMapping
  ResponseEntity<ApiResponse<PageResponse<BuilderStoreSummaryResponse>>> getMyBuilderStores(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @Valid @ParameterObject BuilderStorePageRequest request);

  @Operation(
      summary = "내 가상 점포 상세 조회",
      operationId = "getMyBuilderStore",
      security = @SecurityRequirement(name = "bearerAuth"))
  @ApiErrorCodeExamples({
    ErrorCode.UNAUTHORIZED,
    ErrorCode.NOT_FOUND_USER,
    ErrorCode.NOT_FOUND_BUILDER_STORE,
    ErrorCode.NOT_OWNER_BUILDER_STORE
  })
  @GetMapping("/{builderStoreId}")
  ResponseEntity<ApiResponse<BuilderStoreDetailResponse>> getMyBuilderStore(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @Parameter(description = "가상 점포 ID", example = "1") @PathVariable @Positive
          Long builderStoreId);

  @Operation(
      summary = "저장 가상 점포 카드 목록 조회",
      operationId = "getMyBuilderStoreBookmarks",
      security = @SecurityRequirement(name = "bearerAuth"))
  @ApiErrorCodeExamples({ErrorCode.UNAUTHORIZED, ErrorCode.NOT_FOUND_USER})
  @GetMapping("/bookmarks")
  ResponseEntity<ApiResponse<PageResponse<BuilderStoreBookmarkResponse>>> getBookmarks(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @Valid @ParameterObject BuilderStoreBookmarkPageRequest request);

  @Operation(
      summary = "저장 가상 점포 카드에 최신 데이터 반영",
      operationId = "refreshMyBuilderStoreBookmark",
      security = @SecurityRequirement(name = "bearerAuth"))
  @ApiErrorCodeExamples({
    ErrorCode.UNAUTHORIZED,
    ErrorCode.NOT_BOOKMARKED,
    ErrorCode.ANALYSIS_DATA_COLLECTION_FAILED
  })
  @PostMapping("/bookmarks/{bookmarkId}/refresh")
  ResponseEntity<ApiResponse<BuilderStoreBookmarkResponse>> refreshBookmark(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @Parameter(description = "북마크 ID", example = "1") @PathVariable @Positive Long bookmarkId,
      @Valid @RequestBody(required = false) RefreshBuilderStoreBookmarkRequest request);
}
