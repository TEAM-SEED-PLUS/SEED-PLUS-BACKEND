package seed.seedplusbackend.community.presentation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
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
import seed.seedplusbackend.community.application.CommunityCommandService;
import seed.seedplusbackend.community.application.CommunityQueryService;
import seed.seedplusbackend.community.presentation.dto.CommunityCommentPageRequest;
import seed.seedplusbackend.community.presentation.dto.CommunityCommentResponse;
import seed.seedplusbackend.community.presentation.dto.CommunityPostDetailResponse;
import seed.seedplusbackend.community.presentation.dto.CommunityPostSearchRequest;
import seed.seedplusbackend.community.presentation.dto.CommunityPostSummaryResponse;
import seed.seedplusbackend.community.presentation.dto.CreateCommunityCommentRequest;
import seed.seedplusbackend.community.presentation.dto.SaveCommunityPostRequest;
import seed.seedplusbackend.global.response.ApiResponse;
import seed.seedplusbackend.global.response.PageResponse;
import seed.seedplusbackend.global.security.AuthenticatedUser;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/community/posts")
public class CommunityController implements CommunityApi {
  private final CommunityQueryService queryService;
  private final CommunityCommandService commandService;

  @Override
  public ResponseEntity<ApiResponse<PageResponse<CommunityPostSummaryResponse>>> getPosts(
      @AuthenticationPrincipal AuthenticatedUser user,
      @Valid @ParameterObject CommunityPostSearchRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(
            PageResponse.from(
                queryService.search(request.toQuery(), userId(user)),
                CommunityPostSummaryResponse::from)));
  }

  @Override
  public ResponseEntity<ApiResponse<CommunityPostDetailResponse>> getPost(
      @AuthenticationPrincipal AuthenticatedUser user, @PathVariable @Positive Long postId) {
    return ResponseEntity.ok(
        ApiResponse.success(
            CommunityPostDetailResponse.from(queryService.getPost(postId, userId(user)))));
  }

  @Override
  public ResponseEntity<ApiResponse<CommunityPostDetailResponse>> createPost(
      @AuthenticationPrincipal AuthenticatedUser user,
      @Valid @RequestBody SaveCommunityPostRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            ApiResponse.success(
                HttpStatus.CREATED,
                CommunityPostDetailResponse.from(
                    commandService.create(user.getId(), request.toCommand()))));
  }

  @Override
  public ResponseEntity<ApiResponse<CommunityPostDetailResponse>> updatePost(
      @AuthenticationPrincipal AuthenticatedUser user,
      @PathVariable @Positive Long postId,
      @Valid @RequestBody SaveCommunityPostRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(
            CommunityPostDetailResponse.from(
                commandService.update(user.getId(), postId, request.toCommand()))));
  }

  @Override
  public ResponseEntity<ApiResponse<Void>> deletePost(
      @AuthenticationPrincipal AuthenticatedUser user, @PathVariable @Positive Long postId) {
    commandService.delete(user.getId(), postId);
    return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK));
  }

  @Override
  public ResponseEntity<ApiResponse<PageResponse<CommunityCommentResponse>>> getComments(
      @AuthenticationPrincipal AuthenticatedUser user,
      @PathVariable @Positive Long postId,
      @Valid @ParameterObject CommunityCommentPageRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(
            PageResponse.from(
                queryService.getComments(postId, userId(user), request.toQuery()),
                CommunityCommentResponse::from)));
  }

  @Override
  public ResponseEntity<ApiResponse<CommunityCommentResponse>> createComment(
      @AuthenticationPrincipal AuthenticatedUser user,
      @PathVariable @Positive Long postId,
      @Valid @RequestBody CreateCommunityCommentRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            ApiResponse.success(
                HttpStatus.CREATED,
                CommunityCommentResponse.from(
                    commandService.createComment(user.getId(), postId, request.toCommand()))));
  }

  @Override
  public ResponseEntity<ApiResponse<Void>> deleteComment(
      @AuthenticationPrincipal AuthenticatedUser user,
      @PathVariable @Positive Long postId,
      @PathVariable @Positive Long commentId) {
    commandService.deleteComment(user.getId(), postId, commentId);
    return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK));
  }

  @Override
  public ResponseEntity<ApiResponse<Void>> likePost(
      @AuthenticationPrincipal AuthenticatedUser user, @PathVariable @Positive Long postId) {
    commandService.like(user.getId(), postId);
    return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK));
  }

  @Override
  public ResponseEntity<ApiResponse<Void>> unlikePost(
      @AuthenticationPrincipal AuthenticatedUser user, @PathVariable @Positive Long postId) {
    commandService.unlike(user.getId(), postId);
    return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK));
  }

  private Long userId(AuthenticatedUser user) {
    return user == null ? null : user.getId();
  }
}
