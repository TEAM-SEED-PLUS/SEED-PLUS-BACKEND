package seed.seedplusbackend.domain.entity.feed.enumerate;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NewsCategoryType {
  MARKET("시장뉴스"),         // 시장뉴스
  COMMERCIAL("상권뉴스"),     // 상권뉴스
  TRANSACTION("거래뉴스"),    // 거래뉴스
  POLICY("정책/금리");       // 정책/금리

  private final String displayName;
}
