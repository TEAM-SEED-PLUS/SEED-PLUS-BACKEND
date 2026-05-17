package seed.seedplusbackend.global.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;

@Schema(description = "페이지 응답")
public record PageResponse<T>(
    @Schema(description = "목록") List<T> content,
    @Schema(description = "페이지 정보") PageInfo pageInfo) {

  public static <E, T> PageResponse<T> from(Page<E> page, Function<E, T> mapper) {
    return new PageResponse<>(page.getContent().stream().map(mapper).toList(), PageInfo.from(page));
  }
}
