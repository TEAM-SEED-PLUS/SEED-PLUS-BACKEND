package seed.seedplusbackend.payment.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seed.seedplusbackend.payment.application.result.PaymentHistoryResult;
import seed.seedplusbackend.payment.domain.repository.PaymentRepository;

@Service
@RequiredArgsConstructor
public class PaymentHistoryQueryService {

  private final PaymentRepository paymentRepository;

  @Transactional(readOnly = true)
  public Page<PaymentHistoryResult> getPaymentHistory(Long userId, int page, int size) {
    return paymentRepository
        .findByPaymentOrder_User_IdOrderByCompletedAtDesc(
            userId, PageRequest.of(page, size, Sort.by("id").descending()))
        .map(PaymentHistoryResult::from);
  }
}
