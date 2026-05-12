package seed.seedplusbackend.builderstore.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStore;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreBookmark;
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
@DisplayName("BuilderStoreBookmarkJpaRepository")
class BuilderStoreBookmarkJpaRepositoryTest extends AbstractPostgresContainerTest {

  @Autowired private BuilderStoreBookmarkJpaRepository builderStoreBookmarkJpaRepository;
  @Autowired private BuilderStoreJpaRepository builderStoreJpaRepository;
  @Autowired private UserJpaRepository userJpaRepository;
  @Autowired private RegionJpaRepository regionJpaRepository;
  @Autowired private CommercialAreaJpaRepository commercialAreaJpaRepository;
  @Autowired private IndustryJpaRepository industryJpaRepository;

  @Test
  @DisplayName("가상 점포 북마크를 저장하고 조회할 수 있다")
  void saveAndFindById_smokeTest() {
    User owner = userJpaRepository.save(UserFixture.generalActiveUser("bm-owner@test.com"));
    User bookmarker = userJpaRepository.save(UserFixture.generalActiveUser("bm-user@test.com"));
    Region region = regionJpaRepository.save(RegionFixture.seoulGangnamYeoksamLegalDong());
    CommercialArea area =
        commercialAreaJpaRepository.save(CommercialAreaFixture.developedActive("BM상권"));
    Industry industry = industryJpaRepository.save(IndustryFixture.largeRoot("BM-IND", "기타"));
    BuilderStore builderStore =
        builderStoreJpaRepository.save(
            BuilderStoreFixture.publicBuilderStore(owner, region, area, industry));

    BuilderStoreBookmark saved =
        builderStoreBookmarkJpaRepository.save(
            BuilderStoreBookmark.builder().builderStore(builderStore).user(bookmarker).build());

    assertThat(saved.getId()).isNotNull();
    assertThat(builderStoreBookmarkJpaRepository.findById(saved.getId())).isPresent();
  }
}
