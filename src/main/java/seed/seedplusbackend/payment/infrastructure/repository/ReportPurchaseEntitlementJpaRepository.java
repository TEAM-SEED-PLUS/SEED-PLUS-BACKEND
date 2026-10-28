package seed.seedplusbackend.payment.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import seed.seedplusbackend.payment.domain.entity.ReportPurchaseEntitlement;
import seed.seedplusbackend.payment.domain.repository.ReportPurchaseEntitlementRepository;

public interface ReportPurchaseEntitlementJpaRepository
    extends JpaRepository<ReportPurchaseEntitlement, Long>, ReportPurchaseEntitlementRepository {

  @Override
  boolean existsByUser_IdAndReportId(Long userId, String reportId);
}
