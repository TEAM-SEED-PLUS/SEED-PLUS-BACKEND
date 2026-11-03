package seed.seedplusbackend.payment.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import seed.seedplusbackend.payment.domain.entity.Payment;
import seed.seedplusbackend.payment.domain.repository.PaymentRepository;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long>, PaymentRepository {

  @Override
  <S extends Payment> S save(S entity);

  @Override
  Page<Payment> findByPaymentOrder_User_IdOrderByCompletedAtDesc(Long userId, Pageable pageable);
}
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
