package seed.seedplusbackend.building.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seed.seedplusbackend.building.application.query.BuildingSearchQuery;
import seed.seedplusbackend.building.application.result.BuildingDetailResult;
import seed.seedplusbackend.building.domain.entity.Building;
import seed.seedplusbackend.building.domain.repository.BuildingRepository;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.store.domain.repository.StoreRepository;

@Service
@RequiredArgsConstructor
public class BuildingQueryService {

  private final BuildingRepository buildingRepository;
  private final StoreRepository storeRepository;

  @Transactional(readOnly = true)
  public Page<Building> getBuildings(BuildingSearchQuery query) {
    Pageable pageable = PageRequest.of(query.page(), query.size(), Sort.by("id").ascending());

    if (query.regionId() != null && query.commercialAreaId() != null) {
      return buildingRepository.findByRegion_IdAndCommercialArea_Id(
          query.regionId(), query.commercialAreaId(), pageable);
    }

    if (query.regionId() != null) {
      return buildingRepository.findByRegion_Id(query.regionId(), pageable);
    }

    if (query.commercialAreaId() != null) {
      return buildingRepository.findByCommercialArea_Id(query.commercialAreaId(), pageable);
    }

    return buildingRepository.findAll(pageable);
  }

  @Transactional(readOnly = true)
  public BuildingDetailResult getBuilding(Long buildingId) {
    Building building =
        buildingRepository
            .findWithRegionAndCommercialAreaById(buildingId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND_BUILDING));

    long storeCount = storeRepository.countByBuilding_Id(buildingId);
    long vacantStoreCount = storeRepository.countByBuilding_IdAndVacantTrue(buildingId);

    return new BuildingDetailResult(building, storeCount, vacantStoreCount);
  }
}
