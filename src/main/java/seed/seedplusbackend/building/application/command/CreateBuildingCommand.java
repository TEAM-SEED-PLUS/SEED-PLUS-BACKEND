package seed.seedplusbackend.building.application.command;

import java.math.BigDecimal;

public record CreateBuildingCommand(
    Long regionId,
    Long commercialAreaId,
    String address,
    String name,
    Integer totalFloor,
    BigDecimal totalArea,
    BigDecimal latitude,
    BigDecimal longitude) {}
