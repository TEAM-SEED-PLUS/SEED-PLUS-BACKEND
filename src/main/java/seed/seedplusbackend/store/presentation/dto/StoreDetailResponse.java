package seed.seedplusbackend.store.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import seed.seedplusbackend.building.presentation.dto.BuildingResponse;
import seed.seedplusbackend.industry.presentation.dto.IndustryResponse;
import seed.seedplusbackend.score.domain.entity.ScoreSnapshot;
import seed.seedplusbackend.score.presentation.dto.ScoreSnapshotSummaryResponse;
import seed.seedplusbackend.store.application.StoreDetailResult;
import seed.seedplusbackend.store.domain.entity.Store;

@Schema(description = "점포 상세 응답")
public record StoreDetailResponse(
    @Schema(description = "점포 ID", example = "1") Long storeId,
    @Schema(description = "점포명", example = "시드 카페") String name,
    @Schema(description = "층", example = "1F") String floor,
    @Schema(description = "면적", example = "45") int area,
    @Schema(description = "보증금", example = "10000000") long deposit,
    @Schema(description = "월 임대료", example = "1200000") long monthlyRent,
    @Schema(description = "공실 여부", example = "false") boolean isVacant,
    @Schema(description = "업종명", example = "커피전문점") String industryName,
    @Schema(description = "건물명", example = "시드빌딩") String buildingName,
    @Schema(description = "점포 코드", example = "STORE-001") String code,
    @Schema(description = "건물 정보") BuildingResponse building,
    @Schema(description = "업종 정보") IndustryResponse industry,
    @Schema(description = "최신 점수 요약") ScoreSnapshotSummaryResponse latestScore) {

  public static StoreDetailResponse from(StoreDetailResult result) {
    Store store = result.store();
    ScoreSnapshot latestScore = result.latestScore();

    return new StoreDetailResponse(
        store.getId(),
        store.getName(),
        store.getFloor(),
        store.getArea(),
        store.getDeposit(),
        store.getMonthlyRent(),
        store.isVacant(),
        store.getIndustry().getName(),
        store.getBuilding().getName(),
        store.getCode(),
        BuildingResponse.from(store.getBuilding()),
        IndustryResponse.from(store.getIndustry()),
        latestScore == null ? null : ScoreSnapshotSummaryResponse.from(latestScore));
  }
}
