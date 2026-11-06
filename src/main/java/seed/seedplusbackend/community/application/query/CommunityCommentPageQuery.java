package seed.seedplusbackend.community.application.query;

import org.springframework.data.domain.Pageable;

public record CommunityCommentPageQuery(Pageable pageable) {}
