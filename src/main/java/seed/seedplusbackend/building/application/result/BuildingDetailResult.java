package seed.seedplusbackend.building.application.result;

import seed.seedplusbackend.building.domain.entity.Building;

public record BuildingDetailResult(Building building, long storeCount, long vacantStoreCount) {}
