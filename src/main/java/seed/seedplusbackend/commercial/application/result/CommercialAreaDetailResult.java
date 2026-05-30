package seed.seedplusbackend.commercial.application.result;

import java.util.List;
import seed.seedplusbackend.commercial.domain.entity.CommercialArea;
import seed.seedplusbackend.commercial.domain.entity.CommercialAreaStatus;
import seed.seedplusbackend.commercial.domain.entity.CommercialAreaType;

public record CommercialAreaDetailResult(
    Long commercialAreaId,
    String name,
    CommercialAreaType type,
    CommercialAreaStatus status,
    String description,
    List<CommercialAreaRegionResult> regions,
    CommercialAreaMetricResult latestMetric) {

  public CommercialAreaDetailResult {
    regions = regions == null ? List.of() : List.copyOf(regions);
  }

  public static CommercialAreaDetailResult from(
      CommercialArea commercialArea,
      List<CommercialAreaRegionResult> regions,
      CommercialAreaMetricResult latestMetric) {
    return new CommercialAreaDetailResult(
        commercialArea.getId(),
        commercialArea.getName(),
        commercialArea.getType(),
        commercialArea.getStatus(),
        commercialArea.getDescription(),
        regions,
        latestMetric);
  }
}
