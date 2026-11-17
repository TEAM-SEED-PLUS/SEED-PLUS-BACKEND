package seed.seedplusbackend.payment.domain.repository;

import java.util.Optional;
import seed.seedplusbackend.payment.domain.entity.PaymentProduct;
import seed.seedplusbackend.payment.domain.entity.PaymentProductCode;

public interface PaymentProductRepository {

  Optional<PaymentProduct> findByCodeAndActiveTrue(PaymentProductCode code);
}
