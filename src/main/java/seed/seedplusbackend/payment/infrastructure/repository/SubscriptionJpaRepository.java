package seed.seedplusbackend.payment.infrastructure.repository;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import seed.seedplusbackend.payment.domain.entity.Subscription;
import seed.seedplusbackend.payment.domain.entity.SubscriptionStatus;
import seed.seedplusbackend.payment.domain.repository.SubscriptionRepository;

public interface SubscriptionJpaRepository
    extends JpaRepository<Subscription, Long>, SubscriptionRepository {

  @Override
  Optional<Subscription> findFirstByUser_IdAndStatusOrderByExpiresAtDesc(
      Long userId, SubscriptionStatus status);

  @Override
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<Subscription> findByIdAndUser_Id(Long subscriptionId, Long userId);

  @Override
  <S extends Subscription> S save(S entity);
}
