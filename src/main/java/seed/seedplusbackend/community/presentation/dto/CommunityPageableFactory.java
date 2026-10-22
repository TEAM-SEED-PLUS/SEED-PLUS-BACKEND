package seed.seedplusbackend.community.presentation.dto;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;

final class CommunityPageableFactory {
  private CommunityPageableFactory() {}

  static Pageable posts(Integer page, Integer size, String sort) {
    String value = sort == null || sort.isBlank() ? "createdAt,desc" : sort;
    if (!value.equals("createdAt,desc") && !value.equals("likeCount,desc")) {
      throw new ApplicationException(ErrorCode.INVALID_SORT);
    }
    return PageRequest.of(
        page == null ? 0 : page,
        size == null ? 20 : size,
        Sort.by(Sort.Order.desc(value.split(",")[0]), Sort.Order.desc("id")));
  }

  static Pageable comments(Integer page, Integer size) {
    return PageRequest.of(
        page == null ? 0 : page,
        size == null ? 20 : size,
        Sort.by(Sort.Order.asc("createdAt"), Sort.Order.asc("id")));
  }
}
