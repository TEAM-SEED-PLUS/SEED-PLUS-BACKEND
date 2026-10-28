package seed.seedplusbackend.payment.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.payment.application.command.CreateCardRegistrationCommand;
import seed.seedplusbackend.payment.application.command.GmpayCardRegistrationRequestCommand;
import seed.seedplusbackend.payment.application.port.BillingKeyEncryptionPort;
import seed.seedplusbackend.payment.application.port.GmpayCardRegistrationPort;
import seed.seedplusbackend.payment.application.result.CardRegistrationRequestResult;
import seed.seedplusbackend.payment.application.result.GmpayCardRegistrationRequestResult;
import seed.seedplusbackend.payment.domain.entity.PaymentProduct;
import seed.seedplusbackend.payment.domain.entity.PaymentProductBillingType;
import seed.seedplusbackend.payment.domain.entity.Subscription;
import seed.seedplusbackend.payment.domain.entity.SubscriptionStatus;
import seed.seedplusbackend.payment.domain.repository.PaymentProductRepository;
import seed.seedplusbackend.payment.domain.repository.SubscriptionRepository;
import seed.seedplusbackend.user.domain.entity.User;
import seed.seedplusbackend.user.domain.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class SubscriptionCardRegistrationService {

  private final UserRepository userRepository;
  private final PaymentProductRepository paymentProductRepository;
  private final SubscriptionRepository subscriptionRepository;
  private final GmpayCardRegistrationPort gmpayCardRegistrationPort;
  private final BillingKeyEncryptionPort billingKeyEncryptionPort;

  @Transactional
  public CardRegistrationRequestResult requestCardRegistration(
      Long userId, CreateCardRegistrationCommand command) {
    User user =
        userRepository.findById(userId).orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND_USER));
    PaymentProduct product =
        paymentProductRepository
            .findByCodeAndActiveTrue(command.productCode())
            .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND_PAYMENT_PRODUCT));
    if (product.getBillingType() != PaymentProductBillingType.SUBSCRIPTION) {
      throw new ApplicationException(ErrorCode.INVALID_PAYMENT_ORDER);
    }

    GmpayCardRegistrationRequestResult registrationResult =
        gmpayCardRegistrationPort.requestCardRegistration(
            new GmpayCardRegistrationRequestCommand(
                product.getName(),
                user.getPhoneNumber(),
                String.valueOf(userId),
                user.getName(),
                user.getEmail(),
                product.getCode().name()));
    Subscription subscription =
        subscriptionRepository.save(
            Subscription.builder()
                .user(user)
                .product(product)
                .status(SubscriptionStatus.PENDING)
                .encryptedBillingKey(billingKeyEncryptionPort.encrypt(registrationResult.mpKey()))
                .build());
    return new CardRegistrationRequestResult(subscription.getId(), registrationResult.payUrl());
  }
}
