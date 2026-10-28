package seed.seedplusbackend.payment.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("리포트 구매 권한")
class ReportPurchaseEntitlementTest {

  @Test
  @DisplayName("구매한 리포트에 대해서만 접근 권한을 제공한다")
  void grantsAccessTo_returnsTrueOnlyForPurchasedReport() {
    ReportPurchaseEntitlement entitlement =
        ReportPurchaseEntitlement.builder()
            .reportId("report-1")
            .grantedAt(OffsetDateTime.parse("2026-10-28T12:00:00+09:00"))
            .build();

    assertThat(entitlement.grantsAccessTo("report-1")).isTrue();
    assertThat(entitlement.grantsAccessTo("report-2")).isFalse();
  }
}
