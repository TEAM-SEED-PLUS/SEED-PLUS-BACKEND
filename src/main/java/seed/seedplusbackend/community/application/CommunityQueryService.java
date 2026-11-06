package seed.seedplusbackend.community.application;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seed.seedplusbackend.community.application.port.CommunityPostSearchPort;
import seed.seedplusbackend.community.application.query.CommunityCommentPageQuery;
import seed.seedplusbackend.community.application.query.CommunityPostSearchQuery;
import seed.seedplusbackend.community.application.result.CommunityCommentResult;
import seed.seedplusbackend.community.application.result.CommunityPostResult;
import seed.seedplusbackend.community.domain.CommunityContentPolicy;
import seed.seedplusbackend.community.domain.entity.CommunityPost;
import seed.seedplusbackend.community.domain.repository.CommunityCommentRepository;
import seed.seedplusbackend.community.domain.repository.CommunityLikeRepository;
import seed.seedplusbackend.community.domain.repository.CommunityPostRepository;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityQueryService {
  private final CommunityPostRepository postRepository;
  private final CommunityCommentRepository commentRepository;
  private final CommunityLikeRepository likeRepository;
  private final CommunityPostSearchPort searchPort;
  private final CommunityTagResolver tagResolver;

  public Page<CommunityPostResult> search(CommunityPostSearchQuery query, Long userId) {
    tagResolver.regions(query.regionIds());
    tagResolver.industries(query.industryIds());
    CommunityPostSearchQuery validated =
        new CommunityPostSearchQuery(
            query.keyword(),
            CommunityContentPolicy.tagIds(query.regionIds()),
            CommunityContentPolicy.tagIds(query.industryIds()),
            query.pageable());
    Page<CommunityPost> page = searchPort.search(validated);
    List<Long> ids = page.getContent().stream().map(CommunityPost::getId).toList();
    if (ids.isEmpty()) return page.map(p -> CommunityPostResult.from(p, userId, false));
    postRepository.loadRegions(ids);
    postRepository.loadIndustries(ids);
    Set<Long> liked =
        userId == null ? Set.of() : new HashSet<>(likeRepository.findLikedPostIds(userId, ids));
    return page.map(post -> CommunityPostResult.from(post, userId, liked.contains(post.getId())));
  }

  public CommunityPostResult getPost(Long postId, Long userId) {
    CommunityPost post = getLivePost(postId);
    postRepository.loadRegions(List.of(postId));
    postRepository.loadIndustries(List.of(postId));
    boolean liked =
        userId != null && !likeRepository.findLikedPostIds(userId, List.of(postId)).isEmpty();
    return CommunityPostResult.from(post, userId, liked);
  }

  public Page<CommunityCommentResult> getComments(
      Long postId, Long userId, CommunityCommentPageQuery query) {
    getLivePost(postId);
    return commentRepository
        .findByPost_Id(postId, query.pageable())
        .map(comment -> CommunityCommentResult.from(comment, userId));
  }

  private CommunityPost getLivePost(Long id) {
    return postRepository
        .findById(id)
        .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND_COMMUNITY_POST));
  }
}
