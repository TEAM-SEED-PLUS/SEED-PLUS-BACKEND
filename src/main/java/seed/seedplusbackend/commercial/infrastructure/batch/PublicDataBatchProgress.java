package seed.seedplusbackend.commercial.infrastructure.batch;

final class PublicDataBatchProgress {

  private Long expected;
  private long fetched;

  void update(long total, long count, long cursor) {
    if (total < 0
        || count < fetched
        || count > total
        || cursor < 0
        || (expected != null && expected != total)) {
      throw new IllegalStateException("수집 중 원본 건수 또는 페이지 진행 정보가 변경되었습니다.");
    }
    expected = total;
    fetched = count;
  }

  void verify(long stagedCount) {
    if (expected == null || expected == 0 || fetched != expected || stagedCount != fetched) {
      throw new IllegalStateException("전체 데이터가 수집되지 않아 기존 데이터를 유지합니다.");
    }
  }
}
