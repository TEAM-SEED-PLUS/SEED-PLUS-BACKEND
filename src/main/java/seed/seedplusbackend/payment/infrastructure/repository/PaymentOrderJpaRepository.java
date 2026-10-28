package seed.seedplusbackend.payment.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import seed.seedplusbackend.payment.domain.entity.PaymentOrder;
import seed.seedplusbackend.payment.domain.repository.PaymentOrderRepository;

public interface PaymentOrderJpaRepository
    extends JpaRepository<PaymentOrder, Long>, PaymentOrderRepository {

  @Override
  <S extends PaymentOrder> S save(S entity);
}
