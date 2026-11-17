package seed.seedplusbackend.payment.infrastructure.repository;

import jakarta.persistence.LockModeType;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select s from Subscription s where s.id = :subscriptionId")
  Optional<Subscription> findByIdForUpdate(@Param("subscriptionId") Long subscriptionId);

  @Override
  List<Subscription> findByStatusAndNextBillingAtLessThanEqualOrderByNextBillingAtAsc(
      SubscriptionStatus status, OffsetDateTime nextBillingAt, Pageable pageable);

  @Override
  Optional<Subscription> findFirstByUser_IdOrderByCreatedAtDesc(Long userId);

  @Override
  <S extends Subscription> S save(S entity);

  @Override
  boolean existsByUser_IdAndStatusAndExpiresAtAfter(
      Long userId, SubscriptionStatus status, OffsetDateTime expiresAt);
}
