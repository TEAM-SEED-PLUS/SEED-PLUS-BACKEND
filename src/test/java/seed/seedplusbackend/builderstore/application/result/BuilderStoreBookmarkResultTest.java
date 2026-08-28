package seed.seedplusbackend.builderstore.application.result;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreBookmark;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreBookmarkSnapshot;

@DisplayName("저장 카드 최신화 상태")
class BuilderStoreBookmarkResultTest {

  @Test
  @DisplayName("추정매출 분기가 달라지면 대표 최신화 대상으로 표시한다")
  void marksEstimatedSalesUpdate() {
    BuilderStoreBookmark bookmark = mock(BuilderStoreBookmark.class);
    given(bookmark.getEstimatedSalesQuarter()).willReturn("20261");
    given(bookmark.getBusinessSurvivalYear()).willReturn(2024);
    given(bookmark.getBusinessCountYear()).willReturn(2024);
    given(bookmark.getRentReferenceYear()).willReturn(2026);
    given(bookmark.getRentReferenceQuarter()).willReturn(2);

    BuilderStoreBookmarkResult result =
        BuilderStoreBookmarkResult.of(bookmark, snapshot("20262", 2024, 2024, null, 2026, 2), null);

    assertThat(result.estimatedSalesUpdateAvailable()).isTrue();
    assertThat(result.otherDataUpdateAvailable()).isFalse();
  }

  @Test
  @DisplayName("생존율 등 다른 데이터 기준이 달라지면 변동사항으로 표시한다")
  void marksOtherDataUpdate() {
    OffsetDateTime savedStoreTime = OffsetDateTime.parse("2026-08-01T10:00:00+09:00");
    BuilderStoreBookmark bookmark = mock(BuilderStoreBookmark.class);
    given(bookmark.getEstimatedSalesQuarter()).willReturn("20262");
    given(bookmark.getBusinessSurvivalYear()).willReturn(2023);
    given(bookmark.getBusinessCountYear()).willReturn(2024);
    given(bookmark.getStoreInfoCollectedAt()).willReturn(savedStoreTime);
    given(bookmark.getRentReferenceYear()).willReturn(2026);
    given(bookmark.getRentReferenceQuarter()).willReturn(2);

    BuilderStoreBookmarkResult result =
        BuilderStoreBookmarkResult.of(
            bookmark,
            snapshot("20262", 2024, 2024, savedStoreTime, 2026, 2),
            null);

    assertThat(result.estimatedSalesUpdateAvailable()).isFalse();
    assertThat(result.otherDataUpdateAvailable()).isTrue();
  }

  private BuilderStoreBookmarkSnapshot snapshot(
      String salesQuarter,
      Integer survivalYear,
      Integer countYear,
      OffsetDateTime storeTime,
      Integer rentYear,
      Integer rentQuarter) {
    return new BuilderStoreBookmarkSnapshot(
        salesQuarter,
        null,
        survivalYear,
        null,
        countYear,
        null,
        null,
        null,
        storeTime,
        null,
        rentYear,
        rentQuarter,
        null);
  }
}
