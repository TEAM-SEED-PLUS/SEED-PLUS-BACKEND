package seed.seedplusbackend.payment.infrastructure.repository;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import seed.seedplusbackend.payment.domain.entity.PaymentOrder;
import seed.seedplusbackend.payment.domain.repository.PaymentOrderRepository;

public interface PaymentOrderJpaRepository
    extends JpaRepository<PaymentOrder, Long>, PaymentOrderRepository {

  @Override
  <S extends PaymentOrder> S save(S entity);

  @Override
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<PaymentOrder> findByProviderOrderNo(String providerOrderNo);
}
