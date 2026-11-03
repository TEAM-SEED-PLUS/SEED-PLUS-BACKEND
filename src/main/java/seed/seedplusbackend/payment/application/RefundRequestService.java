package seed.seedplusbackend.payment.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.payment.application.command.CreateRefundRequestCommand;
import seed.seedplusbackend.payment.application.result.RefundRequestResult;
import seed.seedplusbackend.payment.domain.entity.Payment;
import seed.seedplusbackend.payment.domain.entity.PaymentStatus;
import seed.seedplusbackend.payment.domain.entity.RefundRequest;
import seed.seedplusbackend.payment.domain.entity.RefundRequestStatus;
import seed.seedplusbackend.payment.domain.repository.PaymentRepository;
import seed.seedplusbackend.payment.domain.repository.RefundRequestRepository;

@Service
@RequiredArgsConstructor
public class RefundRequestService {

  private final PaymentRepository paymentRepository;
  private final RefundRequestRepository refundRequestRepository;

  @Transactional
  public RefundRequestResult create(Long userId, CreateRefundRequestCommand command) {
    Payment payment =
        paymentRepository
            .findByIdAndPaymentOrder_User_Id(command.paymentId(), userId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.INVALID_REFUNDABLE_PAYMENT));
    if (payment.getStatus() != PaymentStatus.COMPLETED) {
      throw new ApplicationException(ErrorCode.INVALID_REFUNDABLE_PAYMENT);
    }
    if (refundRequestRepository.existsByPayment_Id(payment.getId())) {
      throw new ApplicationException(ErrorCode.DUPLICATE_REFUND_REQUEST);
    }

    RefundRequest refundRequest =
        refundRequestRepository.save(
            RefundRequest.builder()
                .payment(payment)
                .user(payment.getPaymentOrder().getUser())
                .status(RefundRequestStatus.REQUESTED)
                .reason(command.reason())
                .build());
    return RefundRequestResult.from(refundRequest);
  }

  @Transactional(readOnly = true)
  public List<RefundRequestResult> getMyRefundRequests(Long userId) {
    return refundRequestRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
        .map(RefundRequestResult::from)
        .toList();
  }
}
