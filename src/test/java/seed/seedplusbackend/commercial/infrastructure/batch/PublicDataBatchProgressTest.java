package seed.seedplusbackend.commercial.infrastructure.batch;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("배치 수집 완전성 검증")
class PublicDataBatchProgressTest {

  @Test
  @DisplayName("원본 건수와 수집 및 임시 저장 건수가 모두 같아야 완료된다")
  void acceptsCompleteCollection() {
    var progress = new PublicDataBatchProgress();
    progress.update(10, 5, 1);
    progress.update(10, 10, 2);
    assertThatCode(() -> progress.verify(10)).doesNotThrowAnyException();
  }

  @Test
  @DisplayName("중간 페이지가 비거나 저장 건수가 부족하면 완료를 거부한다")
  void rejectsPartialCollection() {
    var progress = new PublicDataBatchProgress();
    progress.update(10, 5, 1);
    assertThatThrownBy(() -> progress.verify(5)).isInstanceOf(IllegalStateException.class);
    progress.update(10, 10, 2);
    assertThatThrownBy(() -> progress.verify(9)).isInstanceOf(IllegalStateException.class);
  }

  @Test
  @DisplayName("수집 중 원본 건수가 바뀌거나 진행 건수가 감소하면 실패한다")
  void rejectsChangingSource() {
    var progress = new PublicDataBatchProgress();
    progress.update(10, 5, 1);
    assertThatThrownBy(() -> progress.update(11, 6, 2)).isInstanceOf(IllegalStateException.class);
    assertThatThrownBy(() -> progress.update(10, 4, 2)).isInstanceOf(IllegalStateException.class);
  }

  @Test
  @DisplayName("진행 정보가 없거나 전체 0건인 응답을 정상 데이터로 반영하지 않는다")
  void rejectsMissingOrEmptyProgress() {
    var progress = new PublicDataBatchProgress();
    assertThatThrownBy(() -> progress.verify(0)).isInstanceOf(IllegalStateException.class);
    progress.update(0, 0, 1);
    assertThatThrownBy(() -> progress.verify(0)).isInstanceOf(IllegalStateException.class);
  }
}
