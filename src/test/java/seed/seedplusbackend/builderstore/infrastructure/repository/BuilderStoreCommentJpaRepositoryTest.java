package seed.seedplusbackend.builderstore.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.util.List;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStore;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreComment;
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
@DisplayName("BuilderStoreCommentJpaRepository")
class BuilderStoreCommentJpaRepositoryTest extends AbstractPostgresContainerTest {

  @Autowired private BuilderStoreCommentJpaRepository builderStoreCommentJpaRepository;
  @Autowired private BuilderStoreJpaRepository builderStoreJpaRepository;
  @Autowired private UserJpaRepository userJpaRepository;
  @Autowired private RegionJpaRepository regionJpaRepository;
  @Autowired private CommercialAreaJpaRepository commercialAreaJpaRepository;
  @Autowired private IndustryJpaRepository industryJpaRepository;
  @Autowired private EntityManager entityManager;

  @Test
  @DisplayName("자기참조 부모-자식 댓글을 저장하고 조회할 수 있다")
  void saveSelfReferencingComments_smokeTest() {
    User owner = userJpaRepository.save(UserFixture.generalActiveUser("c-owner@test.com"));
    User commenter = userJpaRepository.save(UserFixture.generalActiveUser("c-user@test.com"));
    Region region = regionJpaRepository.save(RegionFixture.seoulGangnamYeoksamLegalDong());
    CommercialArea area =
        commercialAreaJpaRepository.save(CommercialAreaFixture.developedActive("CM상권"));
    Industry industry = industryJpaRepository.save(IndustryFixture.largeRoot("CM-IND", "기타"));
    BuilderStore builderStore =
        builderStoreJpaRepository.save(
            BuilderStoreFixture.publicBuilderStore(owner, region, area, industry));

    BuilderStoreComment parent =
        builderStoreCommentJpaRepository.save(
            BuilderStoreComment.builder()
                .builderStore(builderStore)
                .parent(null)
                .user(commenter)
                .content("부모 댓글")
                .build());
    entityManager.flush();
    BuilderStoreComment child =
        builderStoreCommentJpaRepository.save(
            BuilderStoreComment.builder()
                .builderStore(builderStore)
                .parent(parent)
                .user(commenter)
                .content("자식 댓글")
                .build());
    entityManager.flush();
    entityManager.clear();

    BuilderStoreComment refetched =
        builderStoreCommentJpaRepository.findById(child.getId()).orElseThrow();
    Page<BuilderStoreComment> rootComments =
        builderStoreCommentJpaRepository
            .findByBuilderStore_IdAndParentIsNullOrderByCreatedAtAscIdAsc(
                builderStore.getId(), PageRequest.of(0, 10));

    assertThat(refetched.getParent()).isNotNull();
    assertThat(refetched.getParent().getId()).isEqualTo(parent.getId());
    assertThat(rootComments.getContent())
        .extracting(BuilderStoreComment::getId)
        .contains(parent.getId());
    assertThat(rootComments.getContent())
        .extracting(BuilderStoreComment::getId)
        .doesNotContain(child.getId());

    BuilderStoreComment rootComment = rootComments.getContent().get(0);
    List<BuilderStoreComment> replies =
        builderStoreCommentJpaRepository.findByParent_IdOrderByCreatedAtAscIdAsc(parent.getId());
    BuilderStoreComment reply = replies.get(0);

    assertThat(replies).extracting(BuilderStoreComment::getId).containsExactly(child.getId());
    assertThat(Hibernate.isInitialized(rootComment.getUser())).isTrue();
    assertThat(Hibernate.isInitialized(reply.getUser())).isTrue();
    assertThat(Hibernate.isInitialized(reply.getParent())).isTrue();

    entityManager.clear();

    assertThat(rootComment.getUser().getId()).isEqualTo(commenter.getId());
    assertThat(reply.getUser().getId()).isEqualTo(commenter.getId());
    assertThat(reply.getParent().getId()).isEqualTo(parent.getId());
  }
}
