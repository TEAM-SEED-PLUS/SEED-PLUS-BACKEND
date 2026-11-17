package seed.seedplusbackend.payment.infrastructure.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import seed.seedplusbackend.payment.domain.entity.PaymentProduct;
import seed.seedplusbackend.payment.domain.entity.PaymentProductCode;
import seed.seedplusbackend.payment.domain.repository.PaymentProductRepository;

public interface PaymentProductJpaRepository
    extends JpaRepository<PaymentProduct, Long>, PaymentProductRepository {

  @Override
  Optional<PaymentProduct> findByCodeAndActiveTrue(PaymentProductCode code);
}
