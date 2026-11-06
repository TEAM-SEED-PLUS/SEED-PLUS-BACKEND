package seed.seedplusbackend.community.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import seed.seedplusbackend.community.application.result.CommunityAuthorResult;

@Schema(description = "공개 작성자 정보. name 미설정 시 사용자")
public record CommunityAuthorResponse(Long userId, String userName) {
  public static CommunityAuthorResponse from(CommunityAuthorResult r) {
    return new CommunityAuthorResponse(r.userId(), r.userName());
  }
}
