package seed.seedplusbackend.store.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import seed.seedplusbackend.building.domain.entity.Building;
import seed.seedplusbackend.building.infrastructure.repository.BuildingJpaRepository;
import seed.seedplusbackend.commercial.domain.entity.CommercialArea;
import seed.seedplusbackend.commercial.infrastructure.repository.CommercialAreaJpaRepository;
import seed.seedplusbackend.industry.domain.entity.Industry;
import seed.seedplusbackend.industry.infrastructure.repository.IndustryJpaRepository;
import seed.seedplusbackend.region.domain.entity.Region;
import seed.seedplusbackend.region.infrastructure.repository.RegionJpaRepository;
import seed.seedplusbackend.store.domain.entity.Store;
import seed.seedplusbackend.store.domain.entity.StoreImage;
import seed.seedplusbackend.support.AbstractPostgresContainerTest;
import seed.seedplusbackend.support.RepositoryTest;
import seed.seedplusbackend.support.fixture.BuildingFixture;
import seed.seedplusbackend.support.fixture.CommercialAreaFixture;
import seed.seedplusbackend.support.fixture.IndustryFixture;
import seed.seedplusbackend.support.fixture.RegionFixture;
import seed.seedplusbackend.support.fixture.StoreFixture;

@RepositoryTest
@DisplayName("StoreImageJpaRepository")
class StoreImageJpaRepositoryTest extends AbstractPostgresContainerTest {

  @Autowired private StoreImageJpaRepository storeImageJpaRepository;
  @Autowired private StoreJpaRepository storeJpaRepository;
  @Autowired private BuildingJpaRepository buildingJpaRepository;
  @Autowired private RegionJpaRepository regionJpaRepository;
  @Autowired private CommercialAreaJpaRepository commercialAreaJpaRepository;
  @Autowired private IndustryJpaRepository industryJpaRepository;

  @Test
  @DisplayName("점포 이미지를 저장하고 조회할 수 있다")
  void saveAndFindById_smokeTest() {
    Region region = regionJpaRepository.save(RegionFixture.seoulGangnamYeoksamLegalDong());
    CommercialArea area =
        commercialAreaJpaRepository.save(CommercialAreaFixture.developedActive("이미지상권"));
    Building building =
        buildingJpaRepository.save(BuildingFixture.seoulGangnamBuilding(region, area));
    Industry industry = industryJpaRepository.save(IndustryFixture.largeRoot("SI-1", "음식점업"));
    Store store = storeJpaRepository.save(StoreFixture.activeStore(building, industry, "이미지점포"));

    StoreImage image =
        StoreImage.builder()
            .store(store)
            .imageUrl("https://example.com/a.jpg")
            .displayOrder(0)
            .build();
    StoreImage saved = storeImageJpaRepository.save(image);

    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getCreatedAt()).isNotNull();
    assertThat(storeImageJpaRepository.findById(saved.getId())).isPresent();
  }
}
