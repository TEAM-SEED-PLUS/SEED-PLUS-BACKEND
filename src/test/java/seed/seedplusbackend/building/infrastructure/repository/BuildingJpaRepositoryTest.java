package seed.seedplusbackend.building.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
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
        commercialAreaJpaRepository.save(CommercialAreaFixture.developedActive("강남상권"));

    Building saved = buildingJpaRepository.save(BuildingFixture.seoulGangnamBuilding(region, area));

    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getLocation()).isNotNull();
    assertThat(saved.getLocation().getX())
        .isCloseTo(127.0364, org.assertj.core.api.Assertions.within(1e-6));
  }

  @Test
  @DisplayName("ID로 건물을 조회하면 저장된 데이터가 반환된다")
  void findById_returnsSavedBuilding() {
    Region region =
        regionJpaRepository.save(
            Region.builder()
                .sido("서울특별시")
                .sigungu("강남구")
                .dong("삼성동")
                .code("1168010500")
                .codeType(seed.seedplusbackend.region.domain.entity.RegionCodeType.LEGAL_DONG)
                .build());
    CommercialArea area =
        commercialAreaJpaRepository.save(CommercialAreaFixture.developedActive("삼성동상권"));
    Building saved = buildingJpaRepository.save(BuildingFixture.seoulGangnamBuilding(region, area));
    entityManager.flush();
    entityManager.clear();

    Building found = buildingJpaRepository.findById(saved.getId()).orElseThrow();

    assertThat(found.getName()).isEqualTo("테스트 빌딩");
    assertThat(found.getRegion().getId()).isEqualTo(region.getId());
    assertThat(found.getCommercialArea().getId()).isEqualTo(area.getId());
  }

  @Test
  @DisplayName("위치 정보 없이도 건물을 저장할 수 있다")
  void save_persistsBuildingWithoutLocation() {
    Region region = regionJpaRepository.save(RegionFixture.seoulGangnamYeoksamLegalDong());
    CommercialArea area =
        commercialAreaJpaRepository.save(CommercialAreaFixture.developedActive("위치없음상권"));

    Building noLocation =
        Building.builder()
            .commercialArea(area)
            .region(region)
            .address("서울 강남구 어딘가")
            .name("좌표없음 빌딩")
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
        commercialAreaJpaRepository.save(CommercialAreaFixture.developedActive("삭제대상상권"));
    Building saved = buildingJpaRepository.save(BuildingFixture.seoulGangnamBuilding(region, area));
    Long id = saved.getId();

    buildingJpaRepository.deleteById(id);
    entityManager.flush();

    assertThat(buildingJpaRepository.findById(id)).isEmpty();
  }
}
