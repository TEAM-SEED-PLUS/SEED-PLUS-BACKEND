package seed.seedplusbackend.payment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import seed.seedplusbackend.payment.domain.entity.PaymentProductCode;
import seed.seedplusbackend.payment.domain.entity.Subscription;
import seed.seedplusbackend.payment.domain.entity.SubscriptionStatus;
import seed.seedplusbackend.payment.domain.repository.PaymentProductRepository;
import seed.seedplusbackend.payment.domain.repository.SubscriptionRepository;
import seed.seedplusbackend.user.domain.entity.User;
import seed.seedplusbackend.user.domain.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("구독 카드 등록 서비스")
class SubscriptionCardRegistrationServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private PaymentProductRepository paymentProductRepository;
  @Mock private SubscriptionRepository subscriptionRepository;
  @Mock private GmpayCardRegistrationPort gmpayCardRegistrationPort;
  @Mock private BillingKeyEncryptionPort billingKeyEncryptionPort;
  @Mock private User user;
  @Mock private PaymentProduct product;

  @InjectMocks private SubscriptionCardRegistrationService subscriptionCardRegistrationService;

  @Test
  @DisplayName("PRO 상품의 카드 등록 URL을 발급하고 mpKey를 암호화해 저장한다")
  void requestCardRegistration_savesEncryptedBillingKey() {
    given(userRepository.findById(1L)).willReturn(Optional.of(user));
    given(paymentProductRepository.findByCodeAndActiveTrue(PaymentProductCode.PRO_MONTHLY))
        .willReturn(Optional.of(product));
    given(product.getBillingType()).willReturn(PaymentProductBillingType.SUBSCRIPTION);
    given(product.getName()).willReturn("PRO 월간 구독");
    given(product.getCode()).willReturn(PaymentProductCode.PRO_MONTHLY);
    given(user.getPhoneNumber()).willReturn("01012345678");
    given(user.getEmail()).willReturn("user@example.com");
    given(gmpayCardRegistrationPort.requestCardRegistration(any()))
        .willReturn(new GmpayCardRegistrationRequestResult("mp-key", "https://pay.gmpay.kr/card"));
    given(billingKeyEncryptionPort.encrypt("mp-key")).willReturn("encrypted-mp-key");
    given(subscriptionRepository.save(any(Subscription.class))).willAnswer(invocation -> invocation.getArgument(0));

    CardRegistrationRequestResult result =
        subscriptionCardRegistrationService.requestCardRegistration(
            1L, new CreateCardRegistrationCommand(PaymentProductCode.PRO_MONTHLY));

    ArgumentCaptor<GmpayCardRegistrationRequestCommand> requestCaptor =
        ArgumentCaptor.forClass(GmpayCardRegistrationRequestCommand.class);
    ArgumentCaptor<Subscription> subscriptionCaptor = ArgumentCaptor.forClass(Subscription.class);
    then(gmpayCardRegistrationPort).should().requestCardRegistration(requestCaptor.capture());
    then(subscriptionRepository).should().save(subscriptionCaptor.capture());
    assertThat(requestCaptor.getValue().subscriptionProductCode()).isEqualTo("PRO_MONTHLY");
    assertThat(subscriptionCaptor.getValue().getStatus()).isEqualTo(SubscriptionStatus.PENDING);
    assertThat(subscriptionCaptor.getValue().getEncryptedBillingKey()).isEqualTo("encrypted-mp-key");
    assertThat(result.payUrl()).isEqualTo("https://pay.gmpay.kr/card");
  }

  @Test
  @DisplayName("단건결제 상품으로 카드 등록을 요청하면 거절한다")
  void requestCardRegistration_throwsExceptionWhenProductIsNotSubscription() {
    given(userRepository.findById(1L)).willReturn(Optional.of(user));
    given(paymentProductRepository.findByCodeAndActiveTrue(PaymentProductCode.SURVIVAL_REPORT))
        .willReturn(Optional.of(product));
    given(product.getBillingType()).willReturn(PaymentProductBillingType.ONE_TIME);

    assertThatThrownBy(
            () ->
                subscriptionCardRegistrationService.requestCardRegistration(
                    1L, new CreateCardRegistrationCommand(PaymentProductCode.SURVIVAL_REPORT)))
        .isInstanceOfSatisfying(
            ApplicationException.class,
            exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_PAYMENT_ORDER));
    then(gmpayCardRegistrationPort).shouldHaveNoInteractions();
  }
}
