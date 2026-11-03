package seed.seedplusbackend.payment.infrastructure.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import seed.seedplusbackend.payment.domain.entity.RefundRequest;
import seed.seedplusbackend.payment.domain.repository.RefundRequestRepository;

public interface RefundRequestJpaRepository
    extends JpaRepository<RefundRequest, Long>, RefundRequestRepository {

  @Override
  boolean existsByPayment_Id(Long paymentId);

  @Override
  List<RefundRequest> findByUser_IdOrderByCreatedAtDesc(Long userId);

  @Override
  <S extends RefundRequest> S save(S entity);
}
