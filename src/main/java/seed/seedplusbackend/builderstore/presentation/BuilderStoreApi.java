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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import seed.seedplusbackend.builderstore.presentation.dto.BuilderStoreCommentPageRequest;
import seed.seedplusbackend.builderstore.presentation.dto.BuilderStoreCommentResponse;
import seed.seedplusbackend.builderstore.presentation.dto.BuilderStoreDetailResponse;
import seed.seedplusbackend.builderstore.presentation.dto.BuilderStoreSearchRequest;
import seed.seedplusbackend.builderstore.presentation.dto.BuilderStoreSummaryResponse;
import seed.seedplusbackend.builderstore.presentation.dto.CreateBuilderStoreCommentRequest;
import seed.seedplusbackend.builderstore.presentation.dto.CreateBuilderStoreRequest;
import seed.seedplusbackend.builderstore.presentation.dto.UpdateBuilderStoreCommentRequest;
import seed.seedplusbackend.builderstore.presentation.dto.UpdateBuilderStoreRequest;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.global.response.ApiResponse;
import seed.seedplusbackend.global.response.PageResponse;
import seed.seedplusbackend.global.security.AuthenticatedUser;
import seed.seedplusbackend.global.swagger.annotation.ApiErrorCodeExamples;

@Tag(name = "가상 점포", description = "가상 점포 API")
public interface BuilderStoreApi {

  @Operation(summary = "공개 가상 점포 목록 조회", operationId = "getBuilderStores")
  @ApiErrorCodeExamples({ErrorCode.INVALID_PARAMETER, ErrorCode.INVALID_SORT})
  @GetMapping
  ResponseEntity<ApiResponse<PageResponse<BuilderStoreSummaryResponse>>> getBuilderStores(
      @Valid @ParameterObject BuilderStoreSearchRequest request);

  @Operation(summary = "공개 가상 점포 상세 조회", operationId = "getBuilderStore")
  @ApiErrorCodeExamples({ErrorCode.NOT_FOUND_BUILDER_STORE})
  @GetMapping("/{builderStoreId}")
  ResponseEntity<ApiResponse<BuilderStoreDetailResponse>> getBuilderStore(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @Parameter(description = "가상 점포 ID", example = "1") @PathVariable @Positive
          Long builderStoreId);

  @Operation(
      summary = "가상 점포 생성",
      operationId = "createBuilderStore",
      security = @SecurityRequirement(name = "bearerAuth"))
  @ApiErrorCodeExamples({
    ErrorCode.INVALID_PARAMETER,
    ErrorCode.UNAUTHORIZED,
    ErrorCode.NOT_FOUND_USER,
    ErrorCode.NOT_FOUND_REGION,
    ErrorCode.NOT_FOUND_COMMERCIAL_AREA,
    ErrorCode.NOT_FOUND_INDUSTRY,
    ErrorCode.INVALID_BUILDER_STORE_STATUS
  })
  @PostMapping
  ResponseEntity<ApiResponse<BuilderStoreDetailResponse>> createBuilderStore(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @Valid @RequestBody CreateBuilderStoreRequest request);

  @Operation(
      summary = "가상 점포 수정",
      operationId = "updateBuilderStore",
      security = @SecurityRequirement(name = "bearerAuth"))
  @ApiErrorCodeExamples({
    ErrorCode.INVALID_PARAMETER,
    ErrorCode.UNAUTHORIZED,
    ErrorCode.NOT_FOUND_BUILDER_STORE,
    ErrorCode.NOT_OWNER_BUILDER_STORE,
    ErrorCode.NOT_FOUND_REGION,
    ErrorCode.NOT_FOUND_COMMERCIAL_AREA,
    ErrorCode.NOT_FOUND_INDUSTRY,
    ErrorCode.NOT_FOUND_BUILDING,
    ErrorCode.INVALID_BUILDER_STORE_STATUS
  })
  @PatchMapping("/{builderStoreId}")
  ResponseEntity<ApiResponse<BuilderStoreDetailResponse>> updateBuilderStore(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @Parameter(description = "빌더스토어 ID", example = "1") @PathVariable @Positive
          Long builderStoreId,
      @Valid @RequestBody UpdateBuilderStoreRequest request);

  @Operation(
      summary = "가상 점포 삭제",
      operationId = "deleteBuilderStore",
      security = @SecurityRequirement(name = "bearerAuth"))
  @ApiErrorCodeExamples({
    ErrorCode.UNAUTHORIZED,
    ErrorCode.NOT_FOUND_BUILDER_STORE,
    ErrorCode.NOT_OWNER_BUILDER_STORE
  })
  @DeleteMapping("/{builderStoreId}")
  ResponseEntity<ApiResponse<Void>> deleteBuilderStore(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @Parameter(description = "가상 점포 ID", example = "1") @PathVariable @Positive
          Long builderStoreId);

  @Operation(
      summary = "가상 점포 좋아요",
      operationId = "likeBuilderStore",
      security = @SecurityRequirement(name = "bearerAuth"))
  @ApiErrorCodeExamples({
    ErrorCode.UNAUTHORIZED,
    ErrorCode.NOT_FOUND_USER,
    ErrorCode.NOT_FOUND_BUILDER_STORE,
    ErrorCode.ALREADY_LIKED
  })
  @PostMapping("/{builderStoreId}/likes")
  ResponseEntity<ApiResponse<Void>> likeBuilderStore(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @Parameter(description = "빌더스토어 ID", example = "1") @PathVariable @Positive
          Long builderStoreId);

  @Operation(
      summary = "가상 점포 좋아요 취소",
      operationId = "unlikeBuilderStore",
      security = @SecurityRequirement(name = "bearerAuth"))
  @ApiErrorCodeExamples({
    ErrorCode.UNAUTHORIZED,
    ErrorCode.NOT_FOUND_BUILDER_STORE,
    ErrorCode.NOT_LIKED
  })
  @DeleteMapping("/{builderStoreId}/likes")
  ResponseEntity<ApiResponse<Void>> unlikeBuilderStore(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @Parameter(description = "빌더스토어 ID", example = "1") @PathVariable @Positive
          Long builderStoreId);

  @Operation(
      summary = "가상 점포 북마크",
      operationId = "bookmarkBuilderStore",
      security = @SecurityRequirement(name = "bearerAuth"))
  @ApiErrorCodeExamples({
    ErrorCode.UNAUTHORIZED,
    ErrorCode.NOT_FOUND_USER,
    ErrorCode.NOT_FOUND_BUILDER_STORE,
    ErrorCode.ALREADY_BOOKMARKED
  })
  @PostMapping("/{builderStoreId}/bookmarks")
  ResponseEntity<ApiResponse<Void>> bookmarkBuilderStore(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @Parameter(description = "빌더스토어 ID", example = "1") @PathVariable @Positive
          Long builderStoreId);

  @Operation(
      summary = "가상 점포 북마크 취소",
      operationId = "unbookmarkBuilderStore",
      security = @SecurityRequirement(name = "bearerAuth"))
  @ApiErrorCodeExamples({
    ErrorCode.UNAUTHORIZED,
    ErrorCode.NOT_FOUND_BUILDER_STORE,
    ErrorCode.NOT_BOOKMARKED
  })
  @DeleteMapping("/{builderStoreId}/bookmarks")
  ResponseEntity<ApiResponse<Void>> unbookmarkBuilderStore(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @Parameter(description = "빌더스토어 ID", example = "1") @PathVariable @Positive
          Long builderStoreId);

  @Operation(summary = "가상 점포 댓글 목록 조회", operationId = "getBuilderStoreComments")
  @ApiErrorCodeExamples({ErrorCode.NOT_FOUND_BUILDER_STORE})
  @GetMapping("/{builderStoreId}/comments")
  ResponseEntity<ApiResponse<PageResponse<BuilderStoreCommentResponse>>> getComments(
      @Parameter(description = "빌더스토어 ID", example = "1") @PathVariable @Positive
          Long builderStoreId,
      @Valid @ParameterObject BuilderStoreCommentPageRequest request);

  @Operation(
      summary = "가상 점포 댓글 작성",
      operationId = "createBuilderStoreComment",
      security = @SecurityRequirement(name = "bearerAuth"))
  @ApiErrorCodeExamples({
    ErrorCode.INVALID_PARAMETER,
    ErrorCode.UNAUTHORIZED,
    ErrorCode.NOT_FOUND_USER,
    ErrorCode.NOT_FOUND_BUILDER_STORE,
    ErrorCode.INVALID_PARENT_COMMENT
  })
  @PostMapping("/{builderStoreId}/comments")
  ResponseEntity<ApiResponse<BuilderStoreCommentResponse>> createComment(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @Parameter(description = "빌더스토어 ID", example = "1") @PathVariable @Positive
          Long builderStoreId,
      @Valid @RequestBody CreateBuilderStoreCommentRequest request);

  @Operation(
      summary = "가상 점포 댓글 수정",
      operationId = "updateBuilderStoreComment",
      security = @SecurityRequirement(name = "bearerAuth"))
  @ApiErrorCodeExamples({
    ErrorCode.INVALID_PARAMETER,
    ErrorCode.UNAUTHORIZED,
    ErrorCode.NOT_FOUND_BUILDER_STORE,
    ErrorCode.NOT_FOUND_COMMENT,
    ErrorCode.NOT_OWNER_COMMENT
  })
  @PatchMapping("/{builderStoreId}/comments/{commentId}")
  ResponseEntity<ApiResponse<BuilderStoreCommentResponse>> updateComment(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @Parameter(description = "빌더스토어 ID", example = "1") @PathVariable @Positive
          Long builderStoreId,
      @Parameter(description = "댓글 ID", example = "1") @PathVariable @Positive Long commentId,
      @Valid @RequestBody UpdateBuilderStoreCommentRequest request);

  @Operation(
      summary = "가상 점포 댓글 삭제",
      operationId = "deleteBuilderStoreComment",
      security = @SecurityRequirement(name = "bearerAuth"))
  @ApiErrorCodeExamples({
    ErrorCode.UNAUTHORIZED,
    ErrorCode.NOT_FOUND_BUILDER_STORE,
    ErrorCode.NOT_FOUND_COMMENT,
    ErrorCode.NOT_OWNER_COMMENT
  })
  @DeleteMapping("/{builderStoreId}/comments/{commentId}")
  ResponseEntity<ApiResponse<Void>> deleteComment(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @Parameter(description = "빌더스토어 ID", example = "1") @PathVariable @Positive
          Long builderStoreId,
      @Parameter(description = "댓글 ID", example = "1") @PathVariable @Positive Long commentId);
}
