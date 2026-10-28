package seed.seedplusbackend.payment.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("구독")
class SubscriptionTest {

  @Test
  @DisplayName("활성 구독은 만료 전까지 PRO 권한을 제공한다")
  void isActiveAt_returnsTrueBeforeExpiration() {
    OffsetDateTime now = OffsetDateTime.parse("2026-10-28T12:00:00+09:00");
    Subscription subscription =
        Subscription.builder()
            .status(SubscriptionStatus.ACTIVE)
            .expiresAt(now.plusDays(1))
            .build();

    assertThat(subscription.isActiveAt(now)).isTrue();
  }

  @Test
  @DisplayName("활성 구독도 만료 시각부터는 PRO 권한을 제공하지 않는다")
  void isActiveAt_returnsFalseAtExpiration() {
    OffsetDateTime now = OffsetDateTime.parse("2026-10-28T12:00:00+09:00");
    Subscription subscription =
        Subscription.builder().status(SubscriptionStatus.ACTIVE).expiresAt(now).build();

    assertThat(subscription.isActiveAt(now)).isFalse();
  }
}
