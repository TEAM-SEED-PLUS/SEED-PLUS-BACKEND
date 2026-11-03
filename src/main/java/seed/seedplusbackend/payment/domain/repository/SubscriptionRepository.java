package seed.seedplusbackend.payment.domain.repository;

import java.util.Optional;
import seed.seedplusbackend.payment.domain.entity.Subscription;
import seed.seedplusbackend.payment.domain.entity.SubscriptionStatus;

public interface SubscriptionRepository {

  Optional<Subscription> findFirstByUser_IdAndStatusOrderByExpiresAtDesc(
      Long userId, SubscriptionStatus status);

  Optional<Subscription> findByIdAndUser_Id(Long subscriptionId, Long userId);

  <S extends Subscription> S save(S entity);
}
