package seed.seedplusbackend.metrics.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import seed.seedplusbackend.metrics.domain.entity.RealEstateTransaction;
import seed.seedplusbackend.region.domain.entity.Region;
import seed.seedplusbackend.region.infrastructure.repository.RegionJpaRepository;
import seed.seedplusbackend.support.AbstractPostgresContainerTest;
import seed.seedplusbackend.support.RepositoryTest;
import seed.seedplusbackend.support.fixture.RegionFixture;

@RepositoryTest
@DisplayName("RealEstateTransactionJpaRepository")
class RealEstateTransactionJpaRepositoryTest extends AbstractPostgresContainerTest {

  @Autowired private RealEstateTransactionJpaRepository repository;
  @Autowired private RegionJpaRepository regionJpaRepository;

  @Test
  @DisplayName("부동산 거래를 저장하고 조회할 수 있다")
  void saveAndFindById_smokeTest() {
    Region region = regionJpaRepository.save(RegionFixture.seoulGangnamYeoksamLegalDong());

    RealEstateTransaction saved =
        repository.save(
            RealEstateTransaction.builder()
                .region(region)
                .building(null)
                .transactionDate(LocalDate.of(2026, 4, 15))
                .transactionPrice(1_500_000_000L)
                .address("서울 강남구 역삼동 123-1")
                .area(new BigDecimal("84.50"))
                .pricePerArea(17_750_000L)
                .capRate(new BigDecimal("4.20"))
                .sourceName("MOLIT")
                .collectedAt(null)
                .build());

    assertThat(saved.getId()).isNotNull();
    assertThat(repository.findById(saved.getId())).isPresent();
  }
}
