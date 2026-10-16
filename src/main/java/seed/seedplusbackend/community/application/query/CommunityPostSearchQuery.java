package seed.seedplusbackend.community.application.query;

import java.util.List;
import org.springframework.data.domain.Pageable;

public record CommunityPostSearchQuery(
    String keyword, List<Long> regionIds, List<Long> industryIds, Pageable pageable) {}
