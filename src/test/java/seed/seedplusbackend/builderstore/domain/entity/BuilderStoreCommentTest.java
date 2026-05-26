package seed.seedplusbackend.builderstore.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import seed.seedplusbackend.support.fixture.BuilderStoreFixture;
import seed.seedplusbackend.support.fixture.CommercialAreaFixture;
import seed.seedplusbackend.support.fixture.IndustryFixture;
import seed.seedplusbackend.support.fixture.RegionFixture;
import seed.seedplusbackend.support.fixture.UserFixture;
import seed.seedplusbackend.user.domain.entity.User;

@DisplayName("BuilderStoreComment")
class BuilderStoreCommentTest {

  @Test
  @DisplayName("작성자 ID가 일치하면 소유자로 판단한다")
  void isOwnedBy_returnsTrue_whenUserIdMatches() {
    User user = user(1L);
    BuilderStoreComment comment =
        BuilderStoreComment.builder()
            .builderStore(builderStore(user))
            .parent(null)
            .user(user)
            .content("댓글")
            .build();

    assertThat(comment.isOwnedBy(1L)).isTrue();
    assertThat(comment.isOwnedBy(2L)).isFalse();
  }

  @Test
  @DisplayName("부모 댓글이 있으면 대댓글로 판단한다")
  void isReply_returnsTrue_whenParentExists() {
    User user = user(1L);
    BuilderStore builderStore = builderStore(user);
    BuilderStoreComment parent =
        BuilderStoreComment.builder()
            .builderStore(builderStore)
            .parent(null)
            .user(user)
            .content("부모")
            .build();
    BuilderStoreComment reply =
        BuilderStoreComment.builder()
            .builderStore(builderStore)
            .parent(parent)
            .user(user)
            .content("대댓글")
            .build();

    assertThat(parent.isReply()).isFalse();
    assertThat(reply.isReply()).isTrue();
  }

  @Test
  @DisplayName("내용을 수정할 수 있다")
  void updateContent_changesContent() {
    User user = user(1L);
    BuilderStoreComment comment =
        BuilderStoreComment.builder()
            .builderStore(builderStore(user))
            .parent(null)
            .user(user)
            .content("기존 댓글")
            .build();

    comment.updateContent("수정 댓글");

    assertThat(comment.getContent()).isEqualTo("수정 댓글");
  }

  private BuilderStore builderStore(User user) {
    BuilderStore builderStore =
        BuilderStoreFixture.publicBuilderStore(
            user,
            RegionFixture.seoulGangnamYeoksamLegalDong(),
            CommercialAreaFixture.developedActive("상권"),
            IndustryFixture.largeRoot("IND", "음식점업"));
    ReflectionTestUtils.setField(builderStore, "id", 10L);
    return builderStore;
  }

  private User user(Long id) {
    User user = UserFixture.generalActiveUser("01012345678");
    ReflectionTestUtils.setField(user, "id", id);
    return user;
  }
}
