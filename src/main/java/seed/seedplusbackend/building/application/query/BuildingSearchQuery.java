package seed.seedplusbackend.building.application.query;

public record BuildingSearchQuery(int page, int size, Long regionId, Long commercialAreaId) {}
