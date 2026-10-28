package seed.seedplusbackend.payment.domain.repository;

import seed.seedplusbackend.payment.domain.entity.ReportPurchaseEntitlement;

public interface ReportPurchaseEntitlementRepository {

  boolean existsByUser_IdAndReportId(Long userId, String reportId);

  <S extends ReportPurchaseEntitlement> S save(S entity);
}
