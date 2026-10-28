package seed.seedplusbackend.payment.domain.repository;

import seed.seedplusbackend.payment.domain.entity.Payment;

public interface PaymentRepository {

  <S extends Payment> S save(S entity);
}
