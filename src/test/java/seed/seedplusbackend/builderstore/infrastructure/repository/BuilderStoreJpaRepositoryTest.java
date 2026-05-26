package seed.seedplusbackend.builderstore.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStore;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreVisibilityStatus;
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
@DisplayName("BuilderStoreJpaRepository")
class BuilderStoreJpaRepositoryTest extends AbstractPostgresContainerTest {

  @Autowired private BuilderStoreJpaRepository builderStoreJpaRepository;
  @Autowired private UserJpaRepository userJpaRepository;
  @Autowired private RegionJpaRepository regionJpaRepository;
  @Autowired private CommercialAreaJpaRepository commercialAreaJpaRepository;
  @Autowired private IndustryJpaRepository industryJpaRepository;

  @Test
  @DisplayName("가상 점포를 저장하고 조회할 수 있다")
  void saveAndFindById_smokeTest() {
    User user = userJpaRepository.save(UserFixture.generalActiveUser("bs@test.com"));
    Region region = regionJpaRepository.save(RegionFixture.seoulGangnamYeoksamLegalDong());
    CommercialArea area =
        commercialAreaJpaRepository.save(CommercialAreaFixture.developedActive("BS상권"));
    Industry industry = industryJpaRepository.save(IndustryFixture.largeRoot("BS-IND", "음식점업"));

    BuilderStore saved =
        builderStoreJpaRepository.save(
            BuilderStoreFixture.publicBuilderStore(user, region, area, industry));

    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getCreatedAt()).isNotNull();
    assertThat(builderStoreJpaRepository.findById(saved.getId())).isPresent();
  }

  @Test
  @DisplayName("공개 빌더스토어를 필터링하고 정렬해서 조회할 수 있다")
  void searchPublic_filtersAndSortsPublicBuilderStores() {
    User user = userJpaRepository.save(UserFixture.generalActiveUser("search@test.com"));
    Region region = regionJpaRepository.save(RegionFixture.seoulGangnamYeoksamLegalDong());
    CommercialArea area =
        commercialAreaJpaRepository.save(CommercialAreaFixture.developedActive("검색상권"));
    Industry industry = industryJpaRepository.save(IndustryFixture.largeRoot("SEARCH-IND", "음식점업"));
    BuilderStore first = BuilderStoreFixture.publicBuilderStore(user, region, area, industry);
    BuilderStore second = BuilderStoreFixture.publicBuilderStore(user, region, area, industry);
    second.increaseLikeCount();
    builderStoreJpaRepository.save(first);
    builderStoreJpaRepository.save(second);

    Page<BuilderStore> result =
        builderStoreJpaRepository.searchPublic(
            region.getId(),
            area.getId(),
            industry.getId(),
            1,
            100,
            PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "likeCount")));

    assertThat(result.getContent()).hasSizeGreaterThanOrEqualTo(2);
    assertThat(result.getContent().get(0).getLikeCount()).isGreaterThanOrEqualTo(1);
  }

  @Test
  @DisplayName("내 빌더스토어 목록은 DELETED 상태를 제외한다")
  void findMyBuilderStores_excludesDeletedStatus() {
    User user = userJpaRepository.save(UserFixture.generalActiveUser("my-bs@test.com"));
    Region region = regionJpaRepository.save(RegionFixture.seoulGangnamYeoksamLegalDong());
    CommercialArea area =
        commercialAreaJpaRepository.save(CommercialAreaFixture.developedActive("내상권"));
    Industry industry = industryJpaRepository.save(IndustryFixture.largeRoot("MY-BS-IND", "음식점업"));
    BuilderStore active =
        builderStoreJpaRepository.save(
            BuilderStoreFixture.publicBuilderStore(user, region, area, industry));
    BuilderStore deleted =
        builderStoreJpaRepository.save(
            BuilderStoreFixture.publicBuilderStore(user, region, area, industry));
    deleted.delete();

    Page<BuilderStore> result =
        builderStoreJpaRepository.findByUser_IdAndVisibilityStatusNot(
            user.getId(),
            BuilderStoreVisibilityStatus.DELETED,
            PageRequest.of(0, 10, Sort.by("id").ascending()));

    assertThat(result.getContent()).extracting(BuilderStore::getId).contains(active.getId());
    assertThat(result.getContent()).extracting(BuilderStore::getId).doesNotContain(deleted.getId());
  }
}
