package seed.seedplusbackend.builderstore.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStore;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreBookmark;
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

  @Test
  @DisplayName("북마크 목록과 단건 조회에서는 공개 가상 점포만 조회한다")
  void findsOnlyPublicBuilderStoreBookmarks() {
    User owner = userJpaRepository.save(UserFixture.generalActiveUser("visibility-owner@test.com"));
    User bookmarker =
        userJpaRepository.save(UserFixture.generalActiveUser("visibility-user@test.com"));
    Region region = regionJpaRepository.save(RegionFixture.seoulGangnamYeoksamLegalDong());
    CommercialArea area =
        commercialAreaJpaRepository.save(CommercialAreaFixture.developedActive("공개상태상권"));
    Industry industry =
        industryJpaRepository.save(IndustryFixture.largeRoot("VISIBILITY-IND", "공개상태업종"));
    BuilderStore publicStore =
        saveStore(owner, region, area, industry, BuilderStoreVisibilityStatus.PUBLIC);
    BuilderStore privateStore =
        saveStore(owner, region, area, industry, BuilderStoreVisibilityStatus.PRIVATE);
    BuilderStore deletedStore =
        saveStore(owner, region, area, industry, BuilderStoreVisibilityStatus.DELETED);
    BuilderStoreBookmark publicBookmark = saveBookmark(publicStore, bookmarker);
    BuilderStoreBookmark privateBookmark = saveBookmark(privateStore, bookmarker);
    BuilderStoreBookmark deletedBookmark = saveBookmark(deletedStore, bookmarker);

    assertThat(
            builderStoreBookmarkJpaRepository
                .findByUser_IdAndBuilderStore_VisibilityStatusOrderByCreatedAtDesc(
                    bookmarker.getId(), BuilderStoreVisibilityStatus.PUBLIC, PageRequest.of(0, 10))
                .getContent())
        .containsExactly(publicBookmark);
    assertThat(
            builderStoreBookmarkJpaRepository.findByIdAndUser_IdAndBuilderStore_VisibilityStatus(
                publicBookmark.getId(), bookmarker.getId(), BuilderStoreVisibilityStatus.PUBLIC))
        .isPresent();
    assertThat(
            builderStoreBookmarkJpaRepository.findByIdAndUser_IdAndBuilderStore_VisibilityStatus(
                privateBookmark.getId(), bookmarker.getId(), BuilderStoreVisibilityStatus.PUBLIC))
        .isEmpty();
    assertThat(
            builderStoreBookmarkJpaRepository.findByIdAndUser_IdAndBuilderStore_VisibilityStatus(
                deletedBookmark.getId(), bookmarker.getId(), BuilderStoreVisibilityStatus.PUBLIC))
        .isEmpty();
  }

  private BuilderStore saveStore(
      User owner,
      Region region,
      CommercialArea area,
      Industry industry,
      BuilderStoreVisibilityStatus visibilityStatus) {
    BuilderStore store = BuilderStoreFixture.publicBuilderStore(owner, region, area, industry);
    ReflectionTestUtils.setField(store, "visibilityStatus", visibilityStatus);
    return builderStoreJpaRepository.save(store);
  }

  private BuilderStoreBookmark saveBookmark(BuilderStore store, User user) {
    return builderStoreBookmarkJpaRepository.save(
        BuilderStoreBookmark.builder().builderStore(store).user(user).build());
  }
}
