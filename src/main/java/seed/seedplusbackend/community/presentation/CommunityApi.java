package seed.seedplusbackend.community.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
import seed.seedplusbackend.community.presentation.dto.CommunityCommentPageRequest;
import seed.seedplusbackend.community.presentation.dto.CommunityCommentResponse;
import seed.seedplusbackend.community.presentation.dto.CommunityPostDetailResponse;
import seed.seedplusbackend.community.presentation.dto.CommunityPostSearchRequest;
import seed.seedplusbackend.community.presentation.dto.CommunityPostSummaryResponse;
import seed.seedplusbackend.community.presentation.dto.CreateCommunityCommentRequest;
import seed.seedplusbackend.community.presentation.dto.SaveCommunityPostRequest;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.global.response.PageResponse;
import seed.seedplusbackend.global.security.AuthenticatedUser;
import seed.seedplusbackend.global.swagger.annotation.ApiErrorCodeExamples;

@Tag(name = "커뮤니티", description = "조회 공개. 작성·수정·삭제·좋아요는 회원 전용, 수정·삭제는 작성자만 허용")
public interface CommunityApi {
  @Operation(
      summary = "게시글 목록·검색",
      description = "지역·업종 내부 OR, 필터 간 AND. 상위 업종은 하위 태그 포함. 비회원 조회 가능.",
      operationId = "getPostsCommunity")
  @ApiResponse(responseCode = "200", description = "성공")
  @ApiErrorCodeExamples({
    ErrorCode.INVALID_PARAMETER,
    ErrorCode.INVALID_SORT,
    ErrorCode.INVALID_COMMUNITY_TAG,
    ErrorCode.INVALID_TOKEN,
    ErrorCode.EXPIRED_TOKEN
  })
  @GetMapping
  ResponseEntity<
          seed.seedplusbackend.global.response.ApiResponse<
              PageResponse<CommunityPostSummaryResponse>>>
      getPosts(
          @AuthenticationPrincipal AuthenticatedUser user,
          @Valid @ParameterObject CommunityPostSearchRequest request);

  @Operation(
      summary = "게시글 상세",
      description = "삭제된 글은 404. 비회원 liked·isOwner는 false.",
      operationId = "getPostCommunity")
  @ApiResponse(responseCode = "200", description = "성공")
  @ApiErrorCodeExamples({
    ErrorCode.NOT_FOUND_COMMUNITY_POST,
    ErrorCode.INVALID_TOKEN,
    ErrorCode.EXPIRED_TOKEN
  })
  @GetMapping("/{postId}")
  ResponseEntity<seed.seedplusbackend.global.response.ApiResponse<CommunityPostDetailResponse>>
      getPost(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable @Positive Long postId);

  @Operation(
      summary = "게시글 작성",
      description = "태그 선택은 각각 최대 2개. 본문은 일반 텍스트.",
      operationId = "createPostCommunity",
      security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponse(responseCode = "201", description = "성공")
  @ApiErrorCodeExamples({
    ErrorCode.INVALID_REQUEST,
    ErrorCode.INVALID_COMMUNITY_TAG,
    ErrorCode.INVALID_COMMUNITY_CONTENT,
    ErrorCode.INVALID_USER_STATUS,
    ErrorCode.UNAUTHORIZED,
    ErrorCode.INVALID_TOKEN,
    ErrorCode.EXPIRED_TOKEN
  })
  @PostMapping
  ResponseEntity<seed.seedplusbackend.global.response.ApiResponse<CommunityPostDetailResponse>>
      createPost(
          @AuthenticationPrincipal AuthenticatedUser user,
          @Valid @RequestBody SaveCommunityPostRequest request);

  @Operation(
      summary = "본인 게시글 수정",
      description = "전체 편집값을 전달한다. 태그 생략 또는 빈 배열은 기존 태그 제거.",
      operationId = "updatePostCommunity",
      security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponse(responseCode = "200", description = "성공")
  @ApiErrorCodeExamples({
    ErrorCode.NOT_FOUND_COMMUNITY_POST,
    ErrorCode.NOT_OWNER_COMMUNITY_POST,
    ErrorCode.INVALID_REQUEST,
    ErrorCode.INVALID_COMMUNITY_TAG,
    ErrorCode.INVALID_COMMUNITY_CONTENT,
    ErrorCode.UNAUTHORIZED,
    ErrorCode.INVALID_TOKEN,
    ErrorCode.EXPIRED_TOKEN
  })
  @PatchMapping("/{postId}")
  ResponseEntity<seed.seedplusbackend.global.response.ApiResponse<CommunityPostDetailResponse>>
      updatePost(
          @AuthenticationPrincipal AuthenticatedUser user,
          @PathVariable @Positive Long postId,
          @Valid @RequestBody SaveCommunityPostRequest request);

  @Operation(
      summary = "본인 게시글 삭제",
      description = "소프트 삭제. 댓글 기록은 보존하며 삭제된 글에 대한 접근은 404.",
      operationId = "deletePostCommunity",
      security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponse(responseCode = "200", description = "성공")
  @ApiErrorCodeExamples({
    ErrorCode.NOT_FOUND_COMMUNITY_POST,
    ErrorCode.NOT_OWNER_COMMUNITY_POST,
    ErrorCode.UNAUTHORIZED,
    ErrorCode.INVALID_TOKEN,
    ErrorCode.EXPIRED_TOKEN
  })
  @DeleteMapping("/{postId}")
  ResponseEntity<seed.seedplusbackend.global.response.ApiResponse<Void>> deletePost(
      @AuthenticationPrincipal AuthenticatedUser user, @PathVariable @Positive Long postId);

  @Operation(
      summary = "댓글 목록",
      description = "작성 시각·ID 오름차순 페이지 조회. 삭제된 댓글은 제외.",
      operationId = "getCommentsCommunity")
  @ApiResponse(responseCode = "200", description = "성공")
  @ApiErrorCodeExamples({
    ErrorCode.NOT_FOUND_COMMUNITY_POST,
    ErrorCode.INVALID_PARAMETER,
    ErrorCode.INVALID_TOKEN,
    ErrorCode.EXPIRED_TOKEN
  })
  @GetMapping("/{postId}/comments")
  ResponseEntity<
          seed.seedplusbackend.global.response.ApiResponse<PageResponse<CommunityCommentResponse>>>
      getComments(
          @AuthenticationPrincipal AuthenticatedUser user,
          @PathVariable @Positive Long postId,
          @Valid @ParameterObject CommunityCommentPageRequest request);

  @Operation(
      summary = "댓글 작성",
      description = "대댓글은 지원하지 않는다. 최대 2000자 일반 텍스트.",
      operationId = "createCommentCommunity",
      security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponse(responseCode = "201", description = "성공")
  @ApiErrorCodeExamples({
    ErrorCode.NOT_FOUND_COMMUNITY_POST,
    ErrorCode.INVALID_REQUEST,
    ErrorCode.INVALID_COMMUNITY_CONTENT,
    ErrorCode.UNAUTHORIZED,
    ErrorCode.INVALID_TOKEN,
    ErrorCode.EXPIRED_TOKEN
  })
  @PostMapping("/{postId}/comments")
  ResponseEntity<seed.seedplusbackend.global.response.ApiResponse<CommunityCommentResponse>>
      createComment(
          @AuthenticationPrincipal AuthenticatedUser user,
          @PathVariable @Positive Long postId,
          @Valid @RequestBody CreateCommunityCommentRequest request);

  @Operation(
      summary = "본인 댓글 삭제",
      description = "다른 게시글의 댓글 ID 또는 삭제된 댓글은 404.",
      operationId = "deleteCommentCommunity",
      security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponse(responseCode = "200", description = "성공")
  @ApiErrorCodeExamples({
    ErrorCode.NOT_FOUND_COMMUNITY_POST,
    ErrorCode.NOT_FOUND_COMMENT,
    ErrorCode.NOT_OWNER_COMMENT,
    ErrorCode.UNAUTHORIZED,
    ErrorCode.INVALID_TOKEN,
    ErrorCode.EXPIRED_TOKEN
  })
  @DeleteMapping("/{postId}/comments/{commentId}")
  ResponseEntity<seed.seedplusbackend.global.response.ApiResponse<Void>> deleteComment(
      @AuthenticationPrincipal AuthenticatedUser user,
      @PathVariable @Positive Long postId,
      @PathVariable @Positive Long commentId);

  @Operation(
      summary = "좋아요 등록",
      description = "멱등: 이미 등록된 좋아요도 200, 카운트는 한 번만 증가.",
      operationId = "likePostCommunity",
      security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponse(responseCode = "200", description = "성공")
  @ApiErrorCodeExamples({
    ErrorCode.NOT_FOUND_COMMUNITY_POST,
    ErrorCode.UNAUTHORIZED,
    ErrorCode.INVALID_TOKEN,
    ErrorCode.EXPIRED_TOKEN
  })
  @PostMapping("/{postId}/likes")
  ResponseEntity<seed.seedplusbackend.global.response.ApiResponse<Void>> likePost(
      @AuthenticationPrincipal AuthenticatedUser user, @PathVariable @Positive Long postId);

  @Operation(
      summary = "좋아요 취소",
      description = "멱등: 이미 취소된 좋아요도 200, 카운트는 한 번만 감소.",
      operationId = "unlikePostCommunity",
      security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponse(responseCode = "200", description = "성공")
  @ApiErrorCodeExamples({
    ErrorCode.NOT_FOUND_COMMUNITY_POST,
    ErrorCode.UNAUTHORIZED,
    ErrorCode.INVALID_TOKEN,
    ErrorCode.EXPIRED_TOKEN
  })
  @DeleteMapping("/{postId}/likes")
  ResponseEntity<seed.seedplusbackend.global.response.ApiResponse<Void>> unlikePost(
      @AuthenticationPrincipal AuthenticatedUser user, @PathVariable @Positive Long postId);
}
