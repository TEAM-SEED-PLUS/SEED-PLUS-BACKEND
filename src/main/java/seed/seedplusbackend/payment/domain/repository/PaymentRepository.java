package seed.seedplusbackend.payment.domain.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import seed.seedplusbackend.payment.domain.entity.Payment;

public interface PaymentRepository {

  <S extends Payment> S save(S entity);

  Page<Payment> findByPaymentOrder_User_IdOrderByCompletedAtDesc(Long userId, Pageable pageable);

  Optional<Payment> findByIdAndPaymentOrder_User_Id(Long paymentId, Long userId);
}
