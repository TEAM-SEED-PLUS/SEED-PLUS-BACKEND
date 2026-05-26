package seed.seedplusbackend.builderstore.presentation.dto;

import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;

final class BuilderStorePageableFactory {

  private static final Set<String> ALLOWED_SORT_FIELDS =
      Set.of("uploadedAt", "likeCount", "expectedMonthlySales", "expectedProfitRate", "area", "id");
  private static final String DEFAULT_SORT = "uploadedAt,desc";

  private BuilderStorePageableFactory() {}

  static Pageable create(Integer page, Integer size, String sort) {
    int resolvedPage = page == null ? 0 : page;
    int resolvedSize = size == null ? 20 : size;
    Sort resolvedSort = parseSort(sort);
    return PageRequest.of(resolvedPage, resolvedSize, resolvedSort);
  }

  private static Sort parseSort(String sort) {
    String resolvedSort = sort == null || sort.isBlank() ? DEFAULT_SORT : sort;
    String[] parts = resolvedSort.split(",");
    if (parts.length < 1 || parts.length > 2) {
      throw new ApplicationException(ErrorCode.INVALID_SORT);
    }

    String field = parts[0].trim();
    if (!ALLOWED_SORT_FIELDS.contains(field)) {
      throw new ApplicationException(ErrorCode.INVALID_SORT);
    }

    Sort.Direction direction = Sort.Direction.ASC;
    if (parts.length == 2) {
      try {
        direction = Sort.Direction.fromString(parts[1].trim());
      } catch (IllegalArgumentException e) {
        throw new ApplicationException(ErrorCode.INVALID_SORT, e);
      }
    }

    return Sort.by(direction, field);
  }
}
