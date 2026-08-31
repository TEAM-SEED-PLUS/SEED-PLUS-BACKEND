package seed.seedplusbackend.commercial.application;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import seed.seedplusbackend.commercial.application.port.SeoulCommercialEstimatedSalesClientPort;
import seed.seedplusbackend.commercial.application.result.CommercialEstimatedSalesPageResult;
import seed.seedplusbackend.commercial.application.result.CommercialEstimatedSalesRowResult;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;

@Component
@RequiredArgsConstructor
public class LatestEstimatedSalesQuarterResolver {

  private static final ZoneId SEOUL_ZONE_ID = ZoneId.of("Asia/Seoul");
  private static final int LOOKBACK_QUARTER_COUNT = 8;

  private final SeoulCommercialEstimatedSalesClientPort clientPort;

  public String resolve() {
    return resolve(LocalDate.now(SEOUL_ZONE_ID));
  }

  String resolve(LocalDate date) {
    Quarter candidate = Quarter.from(date);

    for (int index = 0; index < LOOKBACK_QUARTER_COUNT; index++) {
      String quarterCode = candidate.code();
      CommercialEstimatedSalesPageResult page = clientPort.fetchByQuarter(quarterCode, 1, 1);

      if (hasData(page, quarterCode)) {
        return quarterCode;
      }

      candidate = candidate.previous();
    }

    throw new ApplicationException(ErrorCode.SEOUL_ESTIMATED_SALES_QUARTER_NOT_FOUND);
  }

  private boolean hasData(CommercialEstimatedSalesPageResult page, String requestedQuarterCode) {
    if (page == null || page.totalCount() < 0 || page.rows() == null) {
      throw new ApplicationException(ErrorCode.SEOUL_OPEN_API_INVALID_RESPONSE);
    }

    List<CommercialEstimatedSalesRowResult> rows = page.rows();
    if (page.totalCount() == 0 && rows.isEmpty()) {
      return false;
    }

    if (page.totalCount() == 0 || rows.isEmpty()) {
      throw new ApplicationException(ErrorCode.SEOUL_OPEN_API_INVALID_RESPONSE);
    }

    boolean containsOnlyRequestedQuarter =
        rows.stream().allMatch(row -> requestedQuarterCode.equals(row.stdrYyquCd()));
    if (!containsOnlyRequestedQuarter) {
      throw new ApplicationException(ErrorCode.SEOUL_OPEN_API_INVALID_RESPONSE);
    }

    return true;
  }

  private record Quarter(int year, int quarter) {

    private static Quarter from(LocalDate date) {
      return new Quarter(date.getYear(), ((date.getMonthValue() - 1) / 3) + 1);
    }

    private String code() {
      return "%d%d".formatted(year, quarter);
    }

    private Quarter previous() {
      return quarter == 1 ? new Quarter(year - 1, 4) : new Quarter(year, quarter - 1);
    }
  }
}
