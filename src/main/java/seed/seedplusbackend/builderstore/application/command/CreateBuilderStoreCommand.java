package seed.seedplusbackend.builderstore.application.command;

import java.util.List;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreVisibilityStatus;

public record CreateBuilderStoreCommand(
    Long regionId,
    Long commercialAreaId,
    Long industryId,
    Long baseBuildingId,
    String name,
    BuilderStoreMetricsCommand metrics,
    String description,
    BuilderStoreVisibilityStatus visibilityStatus,
    List<String> imageUrls) {}
