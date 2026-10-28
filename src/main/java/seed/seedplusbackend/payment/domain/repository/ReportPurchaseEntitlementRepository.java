package seed.seedplusbackend.payment.domain.repository;

public interface ReportPurchaseEntitlementRepository {

  boolean existsByUser_IdAndReportId(Long userId, String reportId);
}
