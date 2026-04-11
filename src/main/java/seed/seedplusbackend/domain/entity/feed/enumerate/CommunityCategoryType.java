package seed.seedplusbackend.domain.entity.feed.enumerate;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommunityCategoryType {
  GROUP_BUY("공동구매"),
  INFO_SHARE("정보공유"),
  DISTRICT_EVENT("상권이벤트"),
  HELP_REQUEST("도움요청");

  private final String displayName;
}
