package seed.seedplusbackend.commercial.application.result;

import java.util.List;

public record PagedCommercialAreaResult(
    List<CommercialAreaResult> content, int page, int size, long totalElements) {

  public PagedCommercialAreaResult {
    content = content == null ? List.of() : List.copyOf(content);
  }
}
