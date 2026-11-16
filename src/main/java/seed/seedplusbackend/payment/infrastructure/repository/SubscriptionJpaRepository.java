package seed.seedplusbackend.payment.infrastructure.repository;

import java.time.OffsetDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import seed.seedplusbackend.payment.domain.entity.Subscription;
import seed.seedplusbackend.payment.domain.entity.SubscriptionStatus;
import seed.seedplusbackend.payment.domain.repository.SubscriptionRepository;

public interface SubscriptionJpaRepository
    extends JpaRepository<Subscription, Long>, SubscriptionRepository {

  @Override
  boolean existsByUser_IdAndStatusAndExpiresAtAfter(
      Long userId, SubscriptionStatus status, OffsetDateTime expiresAt);
}
