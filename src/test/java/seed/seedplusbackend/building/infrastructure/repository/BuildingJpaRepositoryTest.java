package seed.seedplusbackend.building.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import seed.seedplusbackend.building.domain.entity.Building;
import seed.seedplusbackend.commercial.domain.entity.CommercialArea;
import seed.seedplusbackend.commercial.infrastructure.repository.CommercialAreaJpaRepository;
import seed.seedplusbackend.region.domain.entity.Region;
import seed.seedplusbackend.region.infrastructure.repository.RegionJpaRepository;
import seed.seedplusbackend.support.AbstractPostgresContainerTest;
import seed.seedplusbackend.support.RepositoryTest;
import seed.seedplusbackend.support.fixture.BuildingFixture;
import seed.seedplusbackend.support.fixture.CommercialAreaFixture;
import seed.seedplusbackend.support.fixture.RegionFixture;

@RepositoryTest
@DisplayName("BuildingJpaRepository")
class BuildingJpaRepositoryTest extends AbstractPostgresContainerTest {

  @Autowired private BuildingJpaRepository buildingJpaRepository;
  @Autowired private RegionJpaRepository regionJpaRepository;
  @Autowired private CommercialAreaJpaRepository commercialAreaJpaRepository;
  @Autowired private EntityManager entityManager;

  @Test
  @DisplayName("PostGIS Point 좌표를 가진 건물을 저장하면 ID와 location이 보존된다")
  void save_persistsBuildingWithGeographyPoint() {
    Region region = regionJpaRepository.save(RegionFixture.seoulGangnamYeoksamLegalDong());
    CommercialArea area =
        commercialAreaJpaRepository.save(CommercialAreaFixture.developedActive("Gangnam Area"));

    Building saved = buildingJpaRepository.save(BuildingFixture.seoulGangnamBuilding(region, area));

    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getLocation()).isNotNull();
    assertThat(saved.getLocation().getX())
        .isCloseTo(127.0364, org.assertj.core.api.Assertions.within(1e-6));
  }

  @Test
  @DisplayName("같은 지역과 상권의 동일 주소 건물을 조회할 수 있다")
  void findFirstByRegionAndCommercialAreaAndAddress_returnsBuilding() {
    Region region = regionJpaRepository.save(RegionFixture.seoulGangnamYeoksamLegalDong());
    CommercialArea area =
        commercialAreaJpaRepository.save(CommercialAreaFixture.developedActive("Address Area"));
    Building saved = buildingJpaRepository.save(BuildingFixture.seoulGangnamBuilding(region, area));
    entityManager.flush();
    entityManager.clear();

    Building found =
        buildingJpaRepository
            .findFirstByRegion_IdAndCommercialArea_IdAndAddressOrderByIdAsc(
                region.getId(), area.getId(), saved.getAddress())
            .orElseThrow();

    assertThat(found.getId()).isEqualTo(saved.getId());
  }

  @Test
  @DisplayName("좌표 반경 안의 가장 가까운 건물을 조회할 수 있다")
  void findNearestWithinDistance_returnsNearestBuilding() {
    Region region = regionJpaRepository.save(RegionFixture.seoulGangnamYeoksamLegalDong());
    CommercialArea area =
        commercialAreaJpaRepository.save(CommercialAreaFixture.developedActive("Location Area"));
    Building saved = buildingJpaRepository.save(BuildingFixture.seoulGangnamBuilding(region, area));
    entityManager.flush();
    entityManager.clear();

    Building found =
        buildingJpaRepository
            .findNearestWithinDistance(
                region.getId(),
                area.getId(),
                new BigDecimal("37.5012001"),
                new BigDecimal("127.0364001"),
                5.0)
            .orElseThrow();

    assertThat(found.getId()).isEqualTo(saved.getId());
  }

  @Test
  @DisplayName("ID로 건물을 조회하면 저장된 데이터가 반환된다")
  void findById_returnsSavedBuilding() {
    Region region = regionJpaRepository.save(RegionFixture.seoulGangnamYeoksamLegalDong());
    CommercialArea area =
        commercialAreaJpaRepository.save(CommercialAreaFixture.developedActive("Find Area"));
    Building saved = buildingJpaRepository.save(BuildingFixture.seoulGangnamBuilding(region, area));
    entityManager.flush();
    entityManager.clear();

    Building found = buildingJpaRepository.findById(saved.getId()).orElseThrow();

    assertThat(found.getName()).isEqualTo(saved.getName());
    assertThat(found.getRegion().getId()).isEqualTo(region.getId());
    assertThat(found.getCommercialArea().getId()).isEqualTo(area.getId());
  }

  @Test
  @DisplayName("위치 정보 없이도 건물을 저장할 수 있다")
  void save_persistsBuildingWithoutLocation() {
    Region region = regionJpaRepository.save(RegionFixture.seoulGangnamYeoksamLegalDong());
    CommercialArea area =
        commercialAreaJpaRepository.save(CommercialAreaFixture.developedActive("No Location Area"));

    Building noLocation =
        Building.builder()
            .commercialArea(area)
            .region(region)
            .address("서울 강남구 어딘가")
            .name("No Location Building")
            .totalFloor(3)
            .totalArea(null)
            .location(null)
            .build();
    Building saved = buildingJpaRepository.save(noLocation);

    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getLocation()).isNull();
  }

  @Test
  @DisplayName("건물을 삭제하면 더 이상 조회되지 않는다")
  void deleteById_removesBuilding() {
    Region region = regionJpaRepository.save(RegionFixture.seoulGangnamYeoksamLegalDong());
    CommercialArea area =
        commercialAreaJpaRepository.save(CommercialAreaFixture.developedActive("Delete Area"));
    Building saved = buildingJpaRepository.save(BuildingFixture.seoulGangnamBuilding(region, area));
    Long id = saved.getId();

    buildingJpaRepository.deleteById(id);
    entityManager.flush();

    assertThat(buildingJpaRepository.findById(id)).isEmpty();
  }
}
