package seed.seedplusbackend.builderstore.application.command;

import java.util.List;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreVisibilityStatus;
import seed.seedplusbackend.building.application.command.CreateBuildingCommand;

public record CreateBuilderStoreCommand(
    Long regionId,
    Long commercialAreaId,
    Long industryId,
    CreateBuildingCommand building,
    String name,
    BuilderStoreMetricsCommand metrics,
    String description,
    BuilderStoreVisibilityStatus visibilityStatus,
    List<String> imageUrls) {}
