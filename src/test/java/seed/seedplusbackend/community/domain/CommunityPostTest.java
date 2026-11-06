package seed.seedplusbackend.community.domain;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import seed.seedplusbackend.community.domain.entity.CommunityComment;
import seed.seedplusbackend.community.domain.entity.CommunityPost;
import seed.seedplusbackend.global.error.DomainException;
import seed.seedplusbackend.industry.domain.entity.*;
import seed.seedplusbackend.region.domain.entity.*;
import seed.seedplusbackend.support.fixture.UserFixture;

@DisplayName("커뮤니티 입력·상태 정책")
class CommunityPostTest {
  private CommunityPost post(String title, String content, String location) {
    return new CommunityPost(
        UserFixture.generalActiveUser("domain"), title, content, location, Set.of(), Set.of());
  }

  @Test
  @DisplayName("제목·본문·위치 최대 길이와 태그 미선택을 허용한다")
  void acceptsMaximumLengths() {
    CommunityPost p = post("가".repeat(100), "나".repeat(10000), "다".repeat(200));
    assertThat(p.getRegions()).isEmpty();
    assertThat(p.getIndustries()).isEmpty();
    assertThat(new CommunityComment(p, p.getUser(), "라".repeat(2000)).getContent()).hasSize(2000);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "\n\t"})
  @DisplayName("제목과 본문이 비어 있거나 공백이면 거부한다")
  void rejectsBlankPostText(String value) {
    assertThatThrownBy(() -> post(value, "본문", null)).isInstanceOf(DomainException.class);
    assertThatThrownBy(() -> post("제목", value, null)).isInstanceOf(DomainException.class);
  }

  @Test
  @DisplayName("제목·본문·위치 길이 초과를 거부한다")
  void rejectsOversizedPostText() {
    assertThatThrownBy(() -> post("가".repeat(101), "내용", null)).isInstanceOf(DomainException.class);
    assertThatThrownBy(() -> post("제목", "가".repeat(10001), null))
        .isInstanceOf(DomainException.class);
    assertThatThrownBy(() -> post("제목", "내용", "가".repeat(201))).isInstanceOf(DomainException.class);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "\n\t"})
  @DisplayName("공백 댓글은 거부한다")
  void rejectsBlankComment(String value) {
    CommunityPost p = post("제목", "내용", null);
    assertThatThrownBy(() -> new CommunityComment(p, p.getUser(), value))
        .isInstanceOf(DomainException.class);
  }

  @Test
  @DisplayName("댓글 길이 초과를 거부한다")
  void rejectsOversizedComment() {
    CommunityPost p = post("제목", "내용", null);
    assertThatThrownBy(() -> new CommunityComment(p, p.getUser(), "가".repeat(2001)))
        .isInstanceOf(DomainException.class);
  }

  @Test
  @DisplayName("태그 ID 중복·개수 초과·잘못된 ID를 거부한다")
  void rejectsInvalidTagIds() {
    for (List<Long> ids :
        List.of(
            List.of(1L, 1L),
            List.of(1L, 2L, 3L),
            List.of(0L),
            List.of(-1L),
            Arrays.asList(1L, null))) {
      assertThatThrownBy(() -> CommunityContentPolicy.tagIds(ids))
          .isInstanceOf(DomainException.class);
    }
    assertThat(CommunityContentPolicy.tagIds(null)).isEmpty();
    assertThat(CommunityContentPolicy.tagIds(List.of(1L, 2L))).containsExactly(1L, 2L);
  }

  @Test
  @DisplayName("법정동 태그와 비활성 업종 태그를 거부한다")
  void rejectsWrongTagTypes() {
    CommunityPost p = post("제목", "내용", null);
    Region dong =
        Region.builder()
            .sido("서울특별시")
            .sigungu("강남구")
            .dong("역삼동")
            .codeType(RegionCodeType.LEGAL_DONG)
            .build();
    Industry inactive =
        Industry.builder()
            .industryCode("TEST")
            .name("폐업")
            .level(IndustryLevel.LARGE)
            .status(IndustryStatus.INACTIVE)
            .build();
    assertThatThrownBy(() -> p.edit("제목", "내용", null, Set.of(dong), Set.of()))
        .isInstanceOf(DomainException.class);
    assertThatThrownBy(() -> p.edit("제목", "내용", null, Set.of(), Set.of(inactive)))
        .isInstanceOf(DomainException.class);
  }

  @Test
  @DisplayName("카운트는 음수가 될 수 없다")
  void preventsNegativeCounters() {
    CommunityPost p = post("제목", "내용", null);
    assertThatThrownBy(p::removeLike).isInstanceOf(DomainException.class);
    assertThatThrownBy(p::removeComment).isInstanceOf(DomainException.class);
    p.addLike();
    p.addComment();
    p.removeLike();
    p.removeComment();
    assertThat(p.getLikeCount()).isZero();
    assertThat(p.getCommentCount()).isZero();
  }

  @Test
  @DisplayName("글·댓글 삭제는 삭제 시각을 기록한다")
  void deletesSoftly() {
    CommunityPost p = post("제목", "내용", null);
    CommunityComment c = new CommunityComment(p, p.getUser(), "댓글");
    p.delete();
    c.delete();
    assertThat(p.getDeletedAt()).isNotNull();
    assertThat(c.getDeletedAt()).isNotNull();
    assertThat(p.getContent()).isEqualTo("내용");
    assertThat(c.getContent()).isEqualTo("댓글");
  }
}
