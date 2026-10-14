package seed.seedplusbackend.community.application.port;

import org.springframework.data.domain.Page;
import seed.seedplusbackend.community.application.query.CommunityPostSearchQuery;
import seed.seedplusbackend.community.domain.entity.CommunityPost;

public interface CommunityPostSearchPort {
  Page<CommunityPost> search(CommunityPostSearchQuery query);
}
