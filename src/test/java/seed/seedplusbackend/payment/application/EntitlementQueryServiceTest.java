package seed.seedplusbackend.payment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import seed.seedplusbackend.payment.domain.entity.SubscriptionStatus;
import seed.seedplusbackend.payment.domain.repository.ReportPurchaseEntitlementRepository;
import seed.seedplusbackend.payment.domain.repository.SubscriptionRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("권한 조회")
class EntitlementQueryServiceTest {

  @Mock private SubscriptionRepository subscriptionRepository;
  @Mock private ReportPurchaseEntitlementRepository reportPurchaseEntitlementRepository;

  @InjectMocks private EntitlementQueryService entitlementQueryService;

  @Test
  @DisplayName("유효한 PRO 구독과 단건 리포트 구매 권한을 함께 조회한다")
  void getEntitlements_returnsProAndReportPurchase() {
    Long userId = 1L;
    String reportId = "report-1";
    OffsetDateTime now = OffsetDateTime.parse("2026-10-28T12:00:00+09:00");
    given(
            subscriptionRepository.existsByUser_IdAndStatusAndExpiresAtAfter(
                userId, SubscriptionStatus.ACTIVE, now))
        .willReturn(true);
    given(reportPurchaseEntitlementRepository.existsByUser_IdAndReportId(userId, reportId))
        .willReturn(true);

    UserEntitlements result = entitlementQueryService.getEntitlements(userId, reportId, now);

    assertThat(result.plan()).isEqualTo(UserPlan.PRO);
    assertThat(result.pro()).isTrue();
    assertThat(result.reportPurchased()).isTrue();
  }

  @Test
  @DisplayName("구독이 없으면 FREE이며 리포트 구매 권한만 별도로 제공한다")
  void getEntitlements_returnsFreeWithReportPurchase() {
    Long userId = 1L;
    String reportId = "report-1";
    OffsetDateTime now = OffsetDateTime.parse("2026-10-28T12:00:00+09:00");
    given(
            subscriptionRepository.existsByUser_IdAndStatusAndExpiresAtAfter(
                userId, SubscriptionStatus.ACTIVE, now))
        .willReturn(false);
    given(reportPurchaseEntitlementRepository.existsByUser_IdAndReportId(userId, reportId))
        .willReturn(true);

    UserEntitlements result = entitlementQueryService.getEntitlements(userId, reportId, now);

    assertThat(result.plan()).isEqualTo(UserPlan.FREE);
    assertThat(result.pro()).isFalse();
    assertThat(result.reportPurchased()).isTrue();
  }

  @Test
  @DisplayName("만료된 구독은 PRO 권한을 제공하지 않는다")
  void getEntitlements_returnsFreeForExpiredSubscription() {
    Long userId = 1L;
    OffsetDateTime now = OffsetDateTime.parse("2026-10-28T12:00:00+09:00");
    given(
            subscriptionRepository.existsByUser_IdAndStatusAndExpiresAtAfter(
                userId, SubscriptionStatus.ACTIVE, now))
        .willReturn(false);

    UserEntitlements result = entitlementQueryService.getEntitlements(userId, null, now);

    assertThat(result.plan()).isEqualTo(UserPlan.FREE);
    assertThat(result.pro()).isFalse();
    assertThat(result.reportPurchased()).isFalse();
  }
}
