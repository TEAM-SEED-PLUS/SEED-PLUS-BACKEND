package seed.seedplusbackend.commercial.application.result;

import java.time.LocalDateTime;

public record SeoulSdotFootTrafficRowResult(
    String modelName,
    String serialNumber,
    LocalDateTime sensingTime,
    String regionType,
    String autonomousDistrict,
    String administrativeDistrict,
    long visitorCount,
    LocalDateTime registeredAt) {}
