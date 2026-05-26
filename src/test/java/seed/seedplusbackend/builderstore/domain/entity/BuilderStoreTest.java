package seed.seedplusbackend.builderstore.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import seed.seedplusbackend.commercial.domain.entity.CommercialArea;
import seed.seedplusbackend.support.fixture.BuilderStoreFixture;
import seed.seedplusbackend.support.fixture.CommercialAreaFixture;
import seed.seedplusbackend.support.fixture.IndustryFixture;
import seed.seedplusbackend.support.fixture.RegionFixture;
import seed.seedplusbackend.support.fixture.UserFixture;
import seed.seedplusbackend.user.domain.entity.User;

@DisplayName("BuilderStore")
class BuilderStoreTest {

  @Test
  @DisplayName("수정하면 주요 속성과 공개 상태가 변경된다")
  void update_changesFieldsAndVisibilityStatus() {
    BuilderStore builderStore = builderStore();
    CommercialArea nextArea = CommercialAreaFixture.developedActive("다음 상권");

    builderStore.update(
        RegionFixture.seoulGangnamYeoksamLegalDong(),
        nextArea,
        IndustryFixture.largeRoot("NEXT", "소매업"),
        null,
        "수정된 빌더스토어",
        55,
        70_000_000L,
        new BigDecimal("18.20"),
        24,
        3_000_000L,
        30_000_000L,
        120_000_000L,
        "수정 설명",
        BuilderStoreVisibilityStatus.PRIVATE);

    assertThat(builderStore.getName()).isEqualTo("수정된 빌더스토어");
    assertThat(builderStore.getArea()).isEqualTo(55);
    assertThat(builderStore.getExpectedMonthlySales()).isEqualTo(70_000_000L);
    assertThat(builderStore.getPropertyScore()).isEqualTo(80);
    assertThat(builderStore.getVisibilityStatus()).isEqualTo(BuilderStoreVisibilityStatus.PRIVATE);
  }

  @Test
  @DisplayName("삭제하면 DELETED 상태가 된다")
  void delete_changesVisibilityStatusToDeleted() {
    BuilderStore builderStore = builderStore();

    builderStore.delete();

    assertThat(builderStore.isDeleted()).isTrue();
    assertThat(builderStore.getVisibilityStatus()).isEqualTo(BuilderStoreVisibilityStatus.DELETED);
  }

  @Test
  @DisplayName("좋아요 수는 증가하고 0 아래로 감소하지 않는다")
  void likeCount_increasesAndDoesNotGoBelowZero() {
    BuilderStore builderStore = builderStore();

    builderStore.increaseLikeCount();
    builderStore.decreaseLikeCount();
    builderStore.decreaseLikeCount();

    assertThat(builderStore.getLikeCount()).isZero();
  }

  @Test
  @DisplayName("댓글 수 감소는 0 아래로 내려가지 않는다")
  void commentCount_decreaseDoesNotGoBelowZero() {
    BuilderStore builderStore = builderStore();
    builderStore.increaseCommentCount();

    builderStore.decreaseCommentCount(5);

    assertThat(builderStore.getCommentCount()).isZero();
  }

  @Test
  @DisplayName("소유자 ID가 일치하면 소유자로 판단한다")
  void isOwnedBy_returnsTrue_whenUserIdMatches() {
    User user = UserFixture.generalActiveUser("01012345678");
    ReflectionTestUtils.setField(user, "id", 7L);
    BuilderStore builderStore =
        BuilderStoreFixture.publicBuilderStore(
            user,
            RegionFixture.seoulGangnamYeoksamLegalDong(),
            CommercialAreaFixture.developedActive("상권"),
            IndustryFixture.largeRoot("IND", "음식점업"));

    assertThat(builderStore.isOwnedBy(7L)).isTrue();
    assertThat(builderStore.isOwnedBy(8L)).isFalse();
  }

  private BuilderStore builderStore() {
    return BuilderStoreFixture.publicBuilderStore(
        UserFixture.generalActiveUser("01012345678"),
        RegionFixture.seoulGangnamYeoksamLegalDong(),
        CommercialAreaFixture.developedActive("상권"),
        IndustryFixture.largeRoot("IND", "음식점업"));
  }
}
