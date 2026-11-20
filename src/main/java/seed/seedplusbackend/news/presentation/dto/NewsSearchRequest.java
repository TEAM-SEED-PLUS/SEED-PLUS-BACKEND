package seed.seedplusbackend.news.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import seed.seedplusbackend.global.error.*;
import seed.seedplusbackend.news.application.query.NewsSearchQuery;

public record NewsSearchRequest(
    @Schema(allowableValues = {"MARKET", "COMMERCIAL", "TRANSACTION", "POLICY_FINANCE"})
        String category,
    @Size(max = 200) String keyword,
    @Size(max = 10) List<@NotNull @Positive Long> regionIds,
    @Size(max = 10) List<@NotNull @Positive Long> industryIds,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
    @Min(0) Integer page,
    @Min(1) @Max(100) Integer size,
    @Schema(
            defaultValue = "publishedAt,desc",
            allowableValues = {"publishedAt,desc"})
        String sort) {
  public NewsSearchQuery toQuery() {
    if (sort != null && !sort.equals("publishedAt,desc"))
      throw new ApplicationException(ErrorCode.INVALID_SORT);
    return new NewsSearchQuery(
        category,
        keyword,
        regionIds,
        industryIds,
        from,
        to,
        PageRequest.of(page == null ? 0 : page, size == null ? 20 : size));
  }
}
