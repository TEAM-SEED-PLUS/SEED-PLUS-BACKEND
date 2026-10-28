package seed.seedplusbackend.payment.domain.repository;

import seed.seedplusbackend.payment.domain.entity.PaymentOrder;

public interface PaymentOrderRepository {

  <S extends PaymentOrder> S save(S entity);
}
