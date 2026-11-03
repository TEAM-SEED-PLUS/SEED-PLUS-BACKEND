package seed.seedplusbackend.payment.infrastructure.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import seed.seedplusbackend.payment.domain.entity.Payment;
import seed.seedplusbackend.payment.domain.repository.PaymentRepository;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long>, PaymentRepository {

  @Override
  <S extends Payment> S save(S entity);

  @Override
  Page<Payment> findByPaymentOrder_User_IdOrderByCompletedAtDesc(Long userId, Pageable pageable);

  @Override
  Optional<Payment> findByPaymentOrder_Id(Long paymentOrderId);

  @Override
  Optional<Payment> findByIdAndPaymentOrder_User_Id(Long paymentId, Long userId);
}
