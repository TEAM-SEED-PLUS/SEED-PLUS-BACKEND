package seed.seedplusbackend.payment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.payment.application.command.CreateRefundRequestCommand;
import seed.seedplusbackend.payment.application.result.RefundRequestResult;
import seed.seedplusbackend.payment.domain.entity.Payment;
import seed.seedplusbackend.payment.domain.entity.PaymentOrder;
import seed.seedplusbackend.payment.domain.entity.PaymentStatus;
import seed.seedplusbackend.payment.domain.entity.RefundRequest;
import seed.seedplusbackend.payment.domain.entity.RefundRequestStatus;
import seed.seedplusbackend.payment.domain.repository.PaymentRepository;
import seed.seedplusbackend.payment.domain.repository.RefundRequestRepository;
import seed.seedplusbackend.user.domain.entity.User;

@ExtendWith(MockitoExtension.class)
@DisplayName("환불 신청 서비스")
class RefundRequestServiceTest {

  @Mock private PaymentRepository paymentRepository;
  @Mock private RefundRequestRepository refundRequestRepository;
  @Mock private Payment payment;
  @Mock private PaymentOrder paymentOrder;
  @Mock private User user;

  @InjectMocks private RefundRequestService refundRequestService;

  @Test
  @DisplayName("완료된 본인 결제에 대해 환불을 신청한다")
  void create_createsRequestedRefundForCompletedPayment() {
    CreateRefundRequestCommand command = new CreateRefundRequestCommand(10L, "서비스 이용 전 환불 요청");
    given(paymentRepository.findByIdAndPaymentOrder_User_Id(10L, 1L))
        .willReturn(Optional.of(payment));
    given(payment.getId()).willReturn(10L);
    given(payment.getStatus()).willReturn(PaymentStatus.COMPLETED);
    given(payment.getPaymentOrder()).willReturn(paymentOrder);
    given(paymentOrder.getUser()).willReturn(user);
    given(refundRequestRepository.existsByPayment_Id(10L)).willReturn(false);
    given(refundRequestRepository.save(any(RefundRequest.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    RefundRequestResult result = refundRequestService.create(1L, command);

    assertThat(result.paymentId()).isEqualTo(10L);
    assertThat(result.status()).isEqualTo(RefundRequestStatus.REQUESTED);
    assertThat(result.reason()).isEqualTo("서비스 이용 전 환불 요청");
  }

  @Test
  @DisplayName("이미 환불 신청된 결제는 다시 신청할 수 없다")
  void create_throwsExceptionWhenRefundAlreadyExists() {
    CreateRefundRequestCommand command = new CreateRefundRequestCommand(10L, "중복 신청");
    given(paymentRepository.findByIdAndPaymentOrder_User_Id(10L, 1L))
        .willReturn(Optional.of(payment));
    given(payment.getId()).willReturn(10L);
    given(payment.getStatus()).willReturn(PaymentStatus.COMPLETED);
    given(refundRequestRepository.existsByPayment_Id(10L)).willReturn(true);

    assertThatThrownBy(() -> refundRequestService.create(1L, command))
        .isInstanceOf(ApplicationException.class);

    then(refundRequestRepository).should(never()).save(any());
  }
}
