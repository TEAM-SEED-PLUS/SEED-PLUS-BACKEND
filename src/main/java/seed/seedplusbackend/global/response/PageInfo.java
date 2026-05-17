package seed.seedplusbackend.global.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

@Schema(description = "페이지 정보")
public record PageInfo(
    @Schema(description = "현재 페이지 번호", example = "0") int page,
    @Schema(description = "페이지 크기", example = "20") int size,
    @Schema(description = "전체 요소 수", example = "123") long totalElements,
    @Schema(description = "전체 페이지 수", example = "7") int totalPages,
    @Schema(description = "다음 페이지 존재 여부", example = "true") boolean hasNext,
    @Schema(description = "마지막 페이지 여부", example = "false") boolean isLast) {

  public static PageInfo from(Page<?> page) {
    return new PageInfo(
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.hasNext(),
        page.isLast());
  }

  public static PageInfo of(int page, int size, long totalElements) {
    int totalPages = calculateTotalPages(size, totalElements);
    boolean hasNext = page + 1 < totalPages;

    return new PageInfo(page, size, totalElements, totalPages, hasNext, !hasNext);
  }

  private static int calculateTotalPages(int size, long totalElements) {
    if (totalElements == 0) {
      return 0;
    }

    return (int) Math.ceil((double) totalElements / size);
  }
}
