package seed.seedplusbackend.builderstore.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStore;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreRankingSnapshot;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreRankingType;
import seed.seedplusbackend.commercial.domain.entity.CommercialArea;
import seed.seedplusbackend.commercial.infrastructure.repository.CommercialAreaJpaRepository;
import seed.seedplusbackend.industry.domain.entity.Industry;
import seed.seedplusbackend.industry.infrastructure.repository.IndustryJpaRepository;
import seed.seedplusbackend.region.domain.entity.Region;
import seed.seedplusbackend.region.infrastructure.repository.RegionJpaRepository;
import seed.seedplusbackend.support.AbstractPostgresContainerTest;
import seed.seedplusbackend.support.RepositoryTest;
import seed.seedplusbackend.support.fixture.BuilderStoreFixture;
import seed.seedplusbackend.support.fixture.CommercialAreaFixture;
import seed.seedplusbackend.support.fixture.IndustryFixture;
import seed.seedplusbackend.support.fixture.RegionFixture;
import seed.seedplusbackend.support.fixture.UserFixture;
import seed.seedplusbackend.user.domain.entity.User;
import seed.seedplusbackend.user.infrastructure.repository.UserJpaRepository;

@RepositoryTest
@DisplayName("BuilderStoreRankingSnapshotJpaRepository")
class BuilderStoreRankingSnapshotJpaRepositoryTest extends AbstractPostgresContainerTest {

  @Autowired
  private BuilderStoreRankingSnapshotJpaRepository builderStoreRankingSnapshotJpaRepository;

  @Autowired private BuilderStoreJpaRepository builderStoreJpaRepository;
  @Autowired private UserJpaRepository userJpaRepository;
  @Autowired private RegionJpaRepository regionJpaRepository;
  @Autowired private CommercialAreaJpaRepository commercialAreaJpaRepository;
  @Autowired private IndustryJpaRepository industryJpaRepository;

  @Test
  @DisplayName("가상 점포 랭킹 스냅샷을 저장하고 조회할 수 있다")
  void saveAndFindById_smokeTest() {
    User owner = userJpaRepository.save(UserFixture.generalActiveUser("rk-owner@test.com"));
    Region region = regionJpaRepository.save(RegionFixture.seoulGangnamYeoksamLegalDong());
    CommercialArea area =
        commercialAreaJpaRepository.save(CommercialAreaFixture.developedActive("RK상권"));
    Industry industry = industryJpaRepository.save(IndustryFixture.largeRoot("RK-IND", "기타"));
    BuilderStore builderStore =
        builderStoreJpaRepository.save(
            BuilderStoreFixture.publicBuilderStore(owner, region, area, industry));

    BuilderStoreRankingSnapshot saved =
        builderStoreRankingSnapshotJpaRepository.save(
            BuilderStoreRankingSnapshot.builder()
                .builderStore(builderStore)
                .rankingType(BuilderStoreRankingType.SALES)
                .ranking(1)
                .referenceMonth(LocalDate.of(2026, 5, 1))
                .build());

    assertThat(saved.getId()).isNotNull();
    assertThat(builderStoreRankingSnapshotJpaRepository.findById(saved.getId())).isPresent();
  }
}
