package seed.seedplusbackend.payment.domain.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import seed.seedplusbackend.payment.domain.entity.Subscription;
import seed.seedplusbackend.payment.domain.entity.SubscriptionStatus;

public interface SubscriptionRepository {

  Optional<Subscription> findFirstByUser_IdAndStatusOrderByExpiresAtDesc(
      Long userId, SubscriptionStatus status);

  Optional<Subscription> findByIdAndUser_Id(Long subscriptionId, Long userId);

  Optional<Subscription> findByIdForUpdate(Long subscriptionId);

  List<Subscription> findByStatusAndNextBillingAtLessThanEqualOrderByNextBillingAtAsc(
      SubscriptionStatus status, OffsetDateTime nextBillingAt, Pageable pageable);

  Optional<Subscription> findFirstByUser_IdOrderByCreatedAtDesc(Long userId);

  <S extends Subscription> S save(S entity);

  boolean existsByUser_IdAndStatusAndExpiresAtAfter(
      Long userId, SubscriptionStatus status, OffsetDateTime expiresAt);
}
