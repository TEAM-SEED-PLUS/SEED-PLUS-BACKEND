package seed.seedplusbackend.news.application.query;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.news.domain.NewsCategory;

public record NewsSearchQuery(
    String category,
    String keyword,
    List<Long> regionIds,
    List<Long> industryIds,
    LocalDate from,
    LocalDate to,
    Pageable pageable) {
  public NewsSearchQuery {
    try {
      if (category != null) NewsCategory.valueOf(category);
    } catch (IllegalArgumentException e) {
      throw new ApplicationException(ErrorCode.INVALID_PARAMETER);
    }
    regionIds = List.copyOf(regionIds == null ? List.of() : regionIds);
    industryIds = List.copyOf(industryIds == null ? List.of() : industryIds);
    if (from != null && to != null && from.isAfter(to)) {
      throw new ApplicationException(ErrorCode.INVALID_PARAMETER);
    }
  }
}
