package seed.seedplusbackend.payment.infrastructure.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import seed.seedplusbackend.payment.domain.entity.Subscription;
import seed.seedplusbackend.payment.domain.entity.SubscriptionStatus;
import seed.seedplusbackend.payment.domain.repository.SubscriptionRepository;

public interface SubscriptionJpaRepository
    extends JpaRepository<Subscription, Long>, SubscriptionRepository {

  @Override
  Optional<Subscription> findFirstByUser_IdAndStatusOrderByExpiresAtDesc(
      Long userId, SubscriptionStatus status);

  @Override
  <S extends Subscription> S save(S entity);
}
