package seed.seedplusbackend.community.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seed.seedplusbackend.community.application.command.CreateCommunityCommentCommand;
import seed.seedplusbackend.community.application.command.SaveCommunityPostCommand;
import seed.seedplusbackend.community.application.result.CommunityCommentResult;
import seed.seedplusbackend.community.application.result.CommunityPostResult;
import seed.seedplusbackend.community.domain.entity.CommunityComment;
import seed.seedplusbackend.community.domain.entity.CommunityLike;
import seed.seedplusbackend.community.domain.entity.CommunityPost;
import seed.seedplusbackend.community.domain.repository.CommunityCommentRepository;
import seed.seedplusbackend.community.domain.repository.CommunityLikeRepository;
import seed.seedplusbackend.community.domain.repository.CommunityPostRepository;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.user.domain.entity.User;
import seed.seedplusbackend.user.domain.entity.UserStatus;
import seed.seedplusbackend.user.domain.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class CommunityCommandService {
  private final CommunityPostRepository postRepository;
  private final CommunityCommentRepository commentRepository;
  private final CommunityLikeRepository likeRepository;
  private final UserRepository userRepository;
  private final CommunityTagResolver tagResolver;
  private final CommunityQueryService queryService;

  public CommunityPostResult create(Long userId, SaveCommunityPostCommand command) {
    CommunityPost post =
        postRepository.save(
            new CommunityPost(
                getActiveUser(userId),
                command.title(),
                command.content(),
                command.location(),
                tagResolver.regions(command.regionIds()),
                tagResolver.industries(command.industryIds())));
    return queryService.getPost(post.getId(), userId);
  }

  public CommunityPostResult update(Long userId, Long postId, SaveCommunityPostCommand command) {
    CommunityPost post = lockPost(postId);
    getActiveUser(userId);
    requireOwner(post, userId);
    post.edit(
        command.title(),
        command.content(),
        command.location(),
        tagResolver.regions(command.regionIds()),
        tagResolver.industries(command.industryIds()));
    return queryService.getPost(postId, userId);
  }

  public void delete(Long userId, Long postId) {
    CommunityPost post = lockPost(postId);
    getActiveUser(userId);
    requireOwner(post, userId);
    post.delete();
  }

  public CommunityCommentResult createComment(
      Long userId, Long postId, CreateCommunityCommentCommand command) {
    CommunityPost post = lockPost(postId);
    CommunityComment comment =
        commentRepository.save(
            new CommunityComment(post, getActiveUser(userId), command.content()));
    post.addComment();
    return CommunityCommentResult.from(comment, userId);
  }

  public void deleteComment(Long userId, Long postId, Long commentId) {
    CommunityPost post = lockPost(postId);
    getActiveUser(userId);
    CommunityComment comment =
        commentRepository
            .findByIdAndPost_Id(commentId, postId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND_COMMENT));
    if (!comment.isOwner(userId)) throw new ApplicationException(ErrorCode.NOT_OWNER_COMMENT);
    comment.delete();
    post.removeComment();
  }

  public void like(Long userId, Long postId) {
    CommunityPost post = lockPost(postId);
    User user = getActiveUser(userId);
    if (likeRepository.findByPost_IdAndUser_Id(postId, userId).isPresent()) return;
    likeRepository.save(new CommunityLike(post, user));
    post.addLike();
  }

  public void unlike(Long userId, Long postId) {
    CommunityPost post = lockPost(postId);
    getActiveUser(userId);
    likeRepository
        .findByPost_IdAndUser_Id(postId, userId)
        .ifPresent(
            like -> {
              likeRepository.delete(like);
              post.removeLike();
            });
  }

  private CommunityPost lockPost(Long id) {
    return postRepository
        .findLiveForUpdate(id)
        .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND_COMMUNITY_POST));
  }

  private void requireOwner(CommunityPost post, Long userId) {
    if (!post.isOwner(userId)) throw new ApplicationException(ErrorCode.NOT_OWNER_COMMUNITY_POST);
  }

  private User getActiveUser(Long id) {
    User user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND_USER));
    if (user.getStatus() != UserStatus.ACTIVE)
      throw new ApplicationException(ErrorCode.INVALID_USER_STATUS);
    return user;
  }
}
