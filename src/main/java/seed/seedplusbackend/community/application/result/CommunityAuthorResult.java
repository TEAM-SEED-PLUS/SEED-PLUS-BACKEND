package seed.seedplusbackend.community.application.result;

import seed.seedplusbackend.user.domain.entity.User;

public record CommunityAuthorResult(Long userId, String userName) {
  public static CommunityAuthorResult from(User user) {
    String name = user.getName();
    return new CommunityAuthorResult(user.getId(), name == null || name.isBlank() ? "사용자" : name);
  }
}
