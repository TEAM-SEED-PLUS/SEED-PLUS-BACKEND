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
import seed.seedplusbackend.payment.application.command.CreatePaymentOrderCommand;
import seed.seedplusbackend.payment.application.command.GmpayPaymentRequestCommand;
import seed.seedplusbackend.payment.application.port.GmpayPaymentRequestPort;
import seed.seedplusbackend.payment.application.result.GmpayPaymentRequestResult;
import seed.seedplusbackend.payment.application.result.PaymentOrderRequestResult;
import seed.seedplusbackend.payment.domain.entity.PaymentOrder;
import seed.seedplusbackend.payment.domain.entity.PaymentProduct;
import seed.seedplusbackend.payment.domain.entity.PaymentProductBillingType;
import seed.seedplusbackend.payment.domain.entity.PaymentProductCode;
import seed.seedplusbackend.payment.domain.repository.PaymentOrderRepository;
import seed.seedplusbackend.payment.domain.repository.PaymentProductRepository;
import seed.seedplusbackend.user.domain.entity.User;
import seed.seedplusbackend.user.domain.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("결제 주문 서비스")
class PaymentOrderServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private PaymentProductRepository paymentProductRepository;
  @Mock private PaymentOrderRepository paymentOrderRepository;
  @Mock private GmpayPaymentRequestPort gmpayPaymentRequestPort;
  @Mock private User user;
  @Mock private PaymentProduct product;

  @InjectMocks private PaymentOrderService paymentOrderService;

  @Test
  @DisplayName("서버 상품 가격으로 GMPay 결제를 요청한다")
  void requestPayment_requestsGmpayWithProductPrice() {
    given(userRepository.findById(1L)).willReturn(Optional.of(user));
    given(paymentProductRepository.findByCodeAndActiveTrue(PaymentProductCode.SURVIVAL_REPORT))
        .willReturn(Optional.of(product));
    given(product.getBillingType()).willReturn(PaymentProductBillingType.ONE_TIME);
    given(product.getName()).willReturn("생존율 상세 리포트");
    given(product.getAmount()).willReturn(9000L);
    given(product.getCode()).willReturn(PaymentProductCode.SURVIVAL_REPORT);
    given(user.getPhoneNumber()).willReturn("01012345678");
    given(user.getEmail()).willReturn("user@example.com");
    given(paymentOrderRepository.save(any(PaymentOrder.class))).willAnswer(invocation -> invocation.getArgument(0));
    given(gmpayPaymentRequestPort.requestPayment(any()))
        .willReturn(new GmpayPaymentRequestResult("gmpay-order-1", "https://pay.gmpay.kr/1"));

    PaymentOrderRequestResult result =
        paymentOrderService.requestPayment(
            1L, new CreatePaymentOrderCommand(PaymentProductCode.SURVIVAL_REPORT, "report-1"));

    ArgumentCaptor<GmpayPaymentRequestCommand> commandCaptor =
        ArgumentCaptor.forClass(GmpayPaymentRequestCommand.class);
    then(gmpayPaymentRequestPort).should().requestPayment(commandCaptor.capture());
    assertThat(commandCaptor.getValue())
        .extracting(
            GmpayPaymentRequestCommand::goodName,
            GmpayPaymentRequestCommand::totalPrice,
            GmpayPaymentRequestCommand::buyerMobile,
            GmpayPaymentRequestCommand::buyerEmail,
            GmpayPaymentRequestCommand::productCode)
        .containsExactly("생존율 상세 리포트", 9000L, "01012345678", "user@example.com", "SURVIVAL_REPORT");
    assertThat(result.providerOrderNo()).isEqualTo("gmpay-order-1");
    assertThat(result.payUrl()).isEqualTo("https://pay.gmpay.kr/1");
  }

  @Test
  @DisplayName("단건결제에 리포트 식별값이 없으면 결제 요청을 거절한다")
  void requestPayment_throwsExceptionWhenReportIdIsMissing() {
    given(userRepository.findById(1L)).willReturn(Optional.of(user));
    given(paymentProductRepository.findByCodeAndActiveTrue(PaymentProductCode.SURVIVAL_REPORT))
        .willReturn(Optional.of(product));
    given(product.getBillingType()).willReturn(PaymentProductBillingType.ONE_TIME);

    assertThatThrownBy(
            () ->
                paymentOrderService.requestPayment(
                    1L, new CreatePaymentOrderCommand(PaymentProductCode.SURVIVAL_REPORT, null)))
        .isInstanceOfSatisfying(
            ApplicationException.class,
            exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_PAYMENT_ORDER));
    then(gmpayPaymentRequestPort).shouldHaveNoInteractions();
  }

  @Test
  @DisplayName("구독 결제에 리포트 식별값이 포함되면 결제 요청을 거절한다")
  void requestPayment_throwsExceptionWhenSubscriptionHasReportId() {
    given(userRepository.findById(1L)).willReturn(Optional.of(user));
    given(paymentProductRepository.findByCodeAndActiveTrue(PaymentProductCode.PRO_MONTHLY))
        .willReturn(Optional.of(product));
    given(product.getBillingType()).willReturn(PaymentProductBillingType.SUBSCRIPTION);

    assertThatThrownBy(
            () ->
                paymentOrderService.requestPayment(
                    1L, new CreatePaymentOrderCommand(PaymentProductCode.PRO_MONTHLY, "report-1")))
        .isInstanceOfSatisfying(
            ApplicationException.class,
            exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_PAYMENT_ORDER));
    then(gmpayPaymentRequestPort).shouldHaveNoInteractions();
  }
}
