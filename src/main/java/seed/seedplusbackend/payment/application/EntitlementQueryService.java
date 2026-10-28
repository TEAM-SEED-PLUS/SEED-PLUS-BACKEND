package seed.seedplusbackend.payment.application;

import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seed.seedplusbackend.payment.domain.entity.SubscriptionStatus;
import seed.seedplusbackend.payment.domain.repository.ReportPurchaseEntitlementRepository;
import seed.seedplusbackend.payment.domain.repository.SubscriptionRepository;

@Service
@RequiredArgsConstructor
public class EntitlementQueryService {

  private final SubscriptionRepository subscriptionRepository;
  private final ReportPurchaseEntitlementRepository reportPurchaseEntitlementRepository;

  @Transactional(readOnly = true)
  public UserEntitlements getEntitlements(Long userId, String reportId, OffsetDateTime now) {
    boolean pro =
        subscriptionRepository
            .existsByUser_IdAndStatusAndExpiresAtAfter(userId, SubscriptionStatus.ACTIVE, now);
    boolean reportPurchased =
        reportId != null && reportPurchaseEntitlementRepository.existsByUser_IdAndReportId(userId, reportId);
    return new UserEntitlements(pro ? UserPlan.PRO : UserPlan.FREE, pro, reportPurchased);
  }
}
