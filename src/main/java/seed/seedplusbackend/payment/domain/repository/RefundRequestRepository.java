package seed.seedplusbackend.payment.domain.repository;

import java.util.List;
import seed.seedplusbackend.payment.domain.entity.RefundRequest;

public interface RefundRequestRepository {

  boolean existsByPayment_Id(Long paymentId);

  List<RefundRequest> findByUser_IdOrderByCreatedAtDesc(Long userId);

  <S extends RefundRequest> S save(S entity);
}
