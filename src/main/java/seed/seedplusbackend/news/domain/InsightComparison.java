package seed.seedplusbackend.news.domain;

import java.math.*;
import java.time.OffsetDateTime;
import java.util.List;

public record InsightComparison(
    NewsSource source,
    String key,
    String target,
    int year,
    int quarter,
    BigDecimal current,
    BigDecimal previous,
    List<Long> regions,
    List<Long> industries) {
  public boolean comparable() {
    return quarter >= 1
        && quarter <= 4
        && current != null
        && previous != null
        && current.signum() >= 0
        && previous.signum() >= 0;
  }

  public boolean changed() {
    return comparable() && current.compareTo(previous) != 0;
  }

  public NewsItem toItem(OffsetDateTime now) {
    if (!changed())
      throw new seed.seedplusbackend.global.error.DomainException(
          seed.seedplusbackend.global.error.ErrorCode.INVALID_NEWS_ITEM);
    String metric = source == NewsSource.SEOUL_SALES ? "분기 추정매출" : "㎡당 월 임대료";
    String unit = source == NewsSource.SEOUL_SALES ? "원" : "천원/㎡";
    String direction = current.compareTo(previous) > 0 ? "증가" : "감소";
    int previousYear = quarter == 1 ? year - 1 : year;
    int previousQuarter = quarter == 1 ? 4 : quarter - 1;
    String rate =
        previous.signum() == 0
            ? "이전 값이 0이므로 증감률은 계산하지 않습니다."
            : "전분기 대비 "
                + current
                    .subtract(previous)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(previous, 2, RoundingMode.HALF_UP)
                    .toPlainString()
                + "%.";
    String summary = year + "년 " + quarter + "분기 " + metric + " " + direction + ". " + rate;
    String body =
        target
            + " / "
            + summary
            + "\n비교: "
            + previousYear
            + "년 "
            + previousQuarter
            + "분기 "
            + previous.toPlainString()
            + " → "
            + year
            + "년 "
            + quarter
            + "분기 "
            + current.toPlainString()
            + " ("
            + unit
            + ")."
            + "\n차이: "
            + current.subtract(previous).toPlainString()
            + " ("
            + unit
            + "). 출처: "
            + source.label()
            + ". 같은 대상·단위의 전분기 비교이며 변화의 원인을 추정하지 않습니다.";
    return new NewsItem(
        source,
        key,
        year + "년 " + quarter + "분기 " + target + " " + metric + " " + direction,
        summary,
        body,
        source == NewsSource.SEOUL_SALES ? "https://data.seoul.go.kr/" : "https://www.reb.or.kr/",
        now,
        source == NewsSource.SEOUL_SALES ? NewsCategory.COMMERCIAL : NewsCategory.TRANSACTION,
        source == NewsSource.SEOUL_SALES ? "추정매출,상권" : "임대료,상가",
        regions,
        industries);
  }
}
