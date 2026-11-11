package seed.seedplusbackend.payment.domain.repository;

import java.time.OffsetDateTime;
import seed.seedplusbackend.payment.domain.entity.SubscriptionStatus;

public interface SubscriptionRepository {

  boolean existsByUser_IdAndStatusAndExpiresAtAfter(
      Long userId, SubscriptionStatus status, OffsetDateTime expiresAt);
}
