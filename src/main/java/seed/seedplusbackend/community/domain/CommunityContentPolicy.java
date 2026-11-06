package seed.seedplusbackend.community.domain;

import java.util.HashSet;
import java.util.List;
import seed.seedplusbackend.global.error.DomainException;
import seed.seedplusbackend.global.error.ErrorCode;

public final class CommunityContentPolicy {
  private CommunityContentPolicy() {}

  public static void requireText(String text, int maximum) {
    if (text == null || text.isBlank() || text.length() > maximum) {
      throw new DomainException(ErrorCode.INVALID_COMMUNITY_CONTENT);
    }
  }

  public static void requireLocation(String location) {
    if (location != null && location.length() > 200) {
      throw new DomainException(ErrorCode.INVALID_COMMUNITY_CONTENT);
    }
  }

  public static List<Long> tagIds(List<Long> ids) {
    if (ids == null) return List.of();
    if (ids.size() > 2
        || ids.stream().anyMatch(id -> id == null || id <= 0)
        || new HashSet<>(ids).size() != ids.size()) {
      throw new DomainException(ErrorCode.INVALID_COMMUNITY_TAG);
    }
    return List.copyOf(ids);
  }
}
