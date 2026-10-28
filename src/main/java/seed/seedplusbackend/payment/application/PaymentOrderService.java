package seed.seedplusbackend.payment.application;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.payment.application.command.CreatePaymentOrderCommand;
import seed.seedplusbackend.payment.application.command.GmpayPaymentRequestCommand;
import seed.seedplusbackend.payment.application.port.GmpayPaymentRequestPort;
import seed.seedplusbackend.payment.application.result.GmpayPaymentRequestResult;
import seed.seedplusbackend.payment.application.result.PaymentOrderRequestResult;
import seed.seedplusbackend.payment.domain.entity.PaymentOrder;
import seed.seedplusbackend.payment.domain.entity.PaymentOrderStatus;
import seed.seedplusbackend.payment.domain.entity.PaymentProduct;
import seed.seedplusbackend.payment.domain.entity.PaymentProductBillingType;
import seed.seedplusbackend.payment.domain.repository.PaymentOrderRepository;
import seed.seedplusbackend.payment.domain.repository.PaymentProductRepository;
import seed.seedplusbackend.user.domain.entity.User;
import seed.seedplusbackend.user.domain.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class PaymentOrderService {

  private final UserRepository userRepository;
  private final PaymentProductRepository paymentProductRepository;
  private final PaymentOrderRepository paymentOrderRepository;
  private final GmpayPaymentRequestPort gmpayPaymentRequestPort;

  @Transactional
  public PaymentOrderRequestResult requestPayment(Long userId, CreatePaymentOrderCommand command) {
    User user =
        userRepository.findById(userId).orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND_USER));
    PaymentProduct product =
        paymentProductRepository
            .findByCodeAndActiveTrue(command.productCode())
            .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND_PAYMENT_PRODUCT));
    validateReportId(product, command.reportId());

    String orderKey = UUID.randomUUID().toString();
    PaymentOrder paymentOrder =
        paymentOrderRepository.save(
            PaymentOrder.builder()
                .user(user)
                .product(product)
                .orderKey(orderKey)
                .reportId(normalizeReportId(command.reportId()))
                .productName(product.getName())
                .amount(product.getAmount())
                .status(PaymentOrderStatus.READY)
                .build());
    GmpayPaymentRequestResult paymentRequest =
        gmpayPaymentRequestPort.requestPayment(
            new GmpayPaymentRequestCommand(
                product.getName(),
                product.getAmount(),
                user.getPhoneNumber(),
                user.getEmail(),
                orderKey,
                product.getCode().name()));
    paymentOrder.markPaymentRequested(paymentRequest.providerOrderNo(), OffsetDateTime.now());
    return new PaymentOrderRequestResult(
        orderKey, paymentRequest.providerOrderNo(), paymentRequest.payUrl());
  }

  private void validateReportId(PaymentProduct product, String reportId) {
    if (product.getBillingType() == PaymentProductBillingType.SUBSCRIPTION) {
      throw new ApplicationException(ErrorCode.INVALID_PAYMENT_ORDER);
    }
    boolean reportProduct = product.getBillingType() == PaymentProductBillingType.ONE_TIME;
    boolean hasReportId = reportId != null && !reportId.isBlank();
    if (reportProduct != hasReportId) {
      throw new ApplicationException(ErrorCode.INVALID_PAYMENT_ORDER);
    }
  }

  private String normalizeReportId(String reportId) {
    return reportId == null || reportId.isBlank() ? null : reportId;
  }
}
