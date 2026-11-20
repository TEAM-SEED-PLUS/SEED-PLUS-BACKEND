package seed.seedplusbackend.news.infrastructure;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.*;
import seed.seedplusbackend.global.error.ApplicationException;

@DisplayName("뉴스 익명 조회 식별")
class HmacNewsViewerIdentityTest {
  private final HmacNewsViewerIdentity identity =
      new HmacNewsViewerIdentity("test-news-signing-secret-more-than-32-characters");

  @Test
  @DisplayName("서명된 쿠키를 재사용하고 위조된 쿠키는 교체한다")
  void cookie() {
    var first = identity.resolve(null, null);
    assertThat(first.newCookie()).isNotBlank();
    var repeat = identity.resolve(null, first.newCookie());
    assertThat(repeat.viewerHash()).isEqualTo(first.viewerHash());
    assertThat(repeat.newCookie()).isNull();
    var tampered = identity.resolve(null, first.newCookie().substring(0, 37) + "0".repeat(64));
    assertThat(tampered.newCookie()).isNotNull();
    assertThat(tampered.viewerHash()).isNotEqualTo(first.viewerHash());
  }

  @Test
  @DisplayName("회원은 쿠키와 관계없이 사용자 ID로 집계하고 빈 비밀키는 사용하지 않는다")
  void memberAndSecret() {
    assertThat(identity.resolve(5L, null)).isEqualTo(identity.resolve(5L, "bad-cookie"));
    assertThat(identity.resolve(5L, null).viewerHash())
        .isNotEqualTo(identity.resolve(6L, null).viewerHash());
    assertThatThrownBy(() -> new HmacNewsViewerIdentity("").resolve(null, null))
        .isInstanceOf(ApplicationException.class);
  }
}
