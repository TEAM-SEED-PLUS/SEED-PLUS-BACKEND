package seed.seedplusbackend.industry.infrastructure.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import seed.seedplusbackend.industry.domain.entity.Industry;
import seed.seedplusbackend.industry.domain.entity.IndustryStatus;
import seed.seedplusbackend.industry.domain.repository.IndustryRepository;

public interface IndustryJpaRepository extends JpaRepository<Industry, Long>, IndustryRepository {

  @Override
  <S extends Industry> S save(S entity);

  @Override
  Optional<Industry> findById(Long id);

  @Override
  Optional<Industry> findByIndustryCodeAndStatus(String industryCode, IndustryStatus status);

  @Override
  void deleteById(Long id);
}
