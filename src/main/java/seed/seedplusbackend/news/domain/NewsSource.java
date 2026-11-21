package seed.seedplusbackend.news.domain;

public enum NewsSource {
  MSS_PRESS("중소벤처기업부 보도자료", 86),
  MSS_ANNOUNCEMENT("중소벤처기업부 사업공고", 310),
  SEOUL_SALES("서울시 상권분석서비스 추정매출", 0),
  REB_RENT("한국부동산원 소규모 상가 임대료", 0);
  private final String label;
  private final int boardId;

  NewsSource(String label, int boardId) {
    this.label = label;
    this.boardId = boardId;
  }

  public String label() {
    return label;
  }

  public int boardId() {
    return boardId;
  }

  public boolean rss() {
    return boardId != 0;
  }
}
