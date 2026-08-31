package seed.seedplusbackend.analysis.application.command;

import java.util.List;

public record AnalysisCollectionTarget(
    String estimatedSalesQuarter, List<SmallBusinessCollectionTarget> smallBusinessTargets) {

  public AnalysisCollectionTarget {
    if (estimatedSalesQuarter == null || !estimatedSalesQuarter.matches("\\d{4}[1-4]")) {
      throw new IllegalArgumentException("추정매출 기준분기는 YYYYQ 형식이어야 합니다.");
    }
    smallBusinessTargets =
        smallBusinessTargets == null ? List.of() : List.copyOf(smallBusinessTargets);
    if (smallBusinessTargets.isEmpty()) {
      throw new IllegalArgumentException("소상공인 상가정보를 조회할 대상 상권이 없습니다.");
    }
  }
}
