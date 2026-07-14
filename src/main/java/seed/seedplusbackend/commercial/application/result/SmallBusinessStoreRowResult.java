package seed.seedplusbackend.commercial.application.result;

import java.math.BigDecimal;

public record SmallBusinessStoreRowResult(
    String storeId,
    String storeName,
    String branchName,
    String largeIndustryCode,
    String largeIndustryName,
    String mediumIndustryCode,
    String mediumIndustryName,
    String smallIndustryCode,
    String smallIndustryName,
    String standardIndustryCode,
    String standardIndustryName,
    String sidoCode,
    String sidoName,
    String sigunguCode,
    String sigunguName,
    String administrativeDongCode,
    String administrativeDongName,
    String legalDongCode,
    String legalDongName,
    String lotNumberAddress,
    String roadNameAddress,
    String buildingManagementNumber,
    String buildingName,
    String floorNumber,
    String roomNumber,
    BigDecimal longitude,
    BigDecimal latitude) {}
