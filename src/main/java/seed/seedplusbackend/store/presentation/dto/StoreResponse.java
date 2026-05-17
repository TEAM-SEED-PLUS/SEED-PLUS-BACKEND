package seed.seedplusbackend.store.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import seed.seedplusbackend.store.domain.entity.Store;

@Schema(description = "점포 응답")
public record StoreResponse(
    @Schema(description = "점포 ID", example = "1") Long storeId,
    @Schema(description = "점포명", example = "시드 카페") String name,
    @Schema(description = "층", example = "1F") String floor,
    @Schema(description = "면적", example = "45") int area,
    @Schema(description = "보증금", example = "10000000") long deposit,
    @Schema(description = "월 임대료", example = "1200000") long monthlyRent,
    @Schema(description = "공실 여부", example = "false") boolean isVacant,
    @Schema(description = "업종명", example = "커피전문점") String industryName,
    @Schema(description = "건물명", example = "시드빌딩") String buildingName) {

  public static StoreResponse from(Store store) {
    return new StoreResponse(
        store.getId(),
        store.getName(),
        store.getFloor(),
        store.getArea(),
        store.getDeposit(),
        store.getMonthlyRent(),
        store.isVacant(),
        store.getIndustry().getName(),
        store.getBuilding().getName());
  }
}
