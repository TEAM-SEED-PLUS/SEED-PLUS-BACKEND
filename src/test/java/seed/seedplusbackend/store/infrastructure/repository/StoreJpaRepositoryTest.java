package seed.seedplusbackend.store.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import seed.seedplusbackend.building.domain.entity.Building;
import seed.seedplusbackend.building.infrastructure.repository.BuildingJpaRepository;
import seed.seedplusbackend.commercial.domain.entity.CommercialArea;
import seed.seedplusbackend.commercial.infrastructure.repository.CommercialAreaJpaRepository;
import seed.seedplusbackend.industry.domain.entity.Industry;
import seed.seedplusbackend.industry.infrastructure.repository.IndustryJpaRepository;
import seed.seedplusbackend.region.domain.entity.Region;
import seed.seedplusbackend.region.infrastructure.repository.RegionJpaRepository;
import seed.seedplusbackend.store.domain.entity.Store;
import seed.seedplusbackend.store.domain.entity.StoreStatus;
import seed.seedplusbackend.support.AbstractPostgresContainerTest;
import seed.seedplusbackend.support.RepositoryTest;
import seed.seedplusbackend.support.fixture.BuildingFixture;
import seed.seedplusbackend.support.fixture.CommercialAreaFixture;
import seed.seedplusbackend.support.fixture.IndustryFixture;
import seed.seedplusbackend.support.fixture.RegionFixture;
import seed.seedplusbackend.support.fixture.StoreFixture;

@RepositoryTest
@DisplayName("StoreJpaRepository")
class StoreJpaRepositoryTest extends AbstractPostgresContainerTest {

  @Autowired private StoreJpaRepository storeJpaRepository;
  @Autowired private BuildingJpaRepository buildingJpaRepository;
  @Autowired private RegionJpaRepository regionJpaRepository;
  @Autowired private CommercialAreaJpaRepository commercialAreaJpaRepository;
  @Autowired private IndustryJpaRepository industryJpaRepository;
  @Autowired private EntityManager entityManager;

  private Building createBuilding() {
    Region region = regionJpaRepository.save(RegionFixture.seoulGangnamYeoksamLegalDong());
    CommercialArea area =
        commercialAreaJpaRepository.save(CommercialAreaFixture.developedActive("강남상권"));
    return buildingJpaRepository.save(BuildingFixture.seoulGangnamBuilding(region, area));
  }

  private Industry createIndustry(String code) {
    return industryJpaRepository.save(IndustryFixture.largeRoot(code, "음식점업"));
  }

  @Test
  @DisplayName("점포를 저장하면 ID와 createdAt이 자동 부여된다")
  void save_persistsStore() {
    Building building = createBuilding();
    Industry industry = createIndustry("S-IND-1");

    Store saved = storeJpaRepository.save(StoreFixture.activeStore(building, industry, "테스트점포"));

    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getCreatedAt()).isNotNull();
    assertThat(saved.getStatus()).isEqualTo(StoreStatus.ACTIVE);
  }

  @Test
  @DisplayName("ID로 점포를 조회하면 연관 엔티티 식별자가 보존된다")
  void findById_returnsStoreWithAssociations() {
    Building building = createBuilding();
    Industry industry = createIndustry("S-IND-2");
    Store saved = storeJpaRepository.save(StoreFixture.activeStore(building, industry, "조회점포"));
    entityManager.flush();
    entityManager.clear();

    Store found = storeJpaRepository.findById(saved.getId()).orElseThrow();

    assertThat(found.getBuilding().getId()).isEqualTo(building.getId());
    assertThat(found.getIndustry().getId()).isEqualTo(industry.getId());
  }

  @Test
  @DisplayName("같은 code를 중복 저장하면 부분 유니크 인덱스에 의해 무결성 예외가 발생한다")
  void save_throwsDataIntegrityViolation_whenCodeDuplicates() {
    Building building = createBuilding();
    Industry industry = createIndustry("S-IND-3");
    Store first =
        Store.builder()
            .building(building)
            .industry(industry)
            .floor("1F")
            .name("점포A")
            .code("DUP-CODE")
            .area(40)
            .deposit(10_000_000L)
            .monthlyRent(1_500_000L)
            .vacant(false)
            .status(StoreStatus.ACTIVE)
            .build();
    storeJpaRepository.save(first);
    entityManager.flush();

    Store second =
        Store.builder()
            .building(building)
            .industry(industry)
            .floor("2F")
            .name("점포B")
            .code("DUP-CODE")
            .area(40)
            .deposit(10_000_000L)
            .monthlyRent(1_500_000L)
            .vacant(false)
            .status(StoreStatus.ACTIVE)
            .build();

    assertThatThrownBy(
            () -> {
              storeJpaRepository.save(second);
              entityManager.flush();
            })
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  @DisplayName("점포를 저장하면 BaseTimeEntity의 createdAt이 자동 채워지고 updatedAt은 초기에는 null이다")
  void save_setsCreatedAtAutomatically() {
    Building building = createBuilding();
    Industry industry = createIndustry("S-IND-4");
    Store saved = storeJpaRepository.save(StoreFixture.activeStore(building, industry, "감사점포"));
    entityManager.flush();
    entityManager.clear();

    Store refetched = storeJpaRepository.findById(saved.getId()).orElseThrow();
    assertThat(refetched.getCreatedAt()).isNotNull();
    assertThat(refetched.getUpdatedAt()).isNull();
  }

  @Test
  @DisplayName("점포를 삭제하면 더 이상 조회되지 않는다")
  void deleteById_removesStore() {
    Building building = createBuilding();
    Industry industry = createIndustry("S-IND-5");
    Store saved = storeJpaRepository.save(StoreFixture.activeStore(building, industry, "삭제대상점포"));
    Long id = saved.getId();

    storeJpaRepository.deleteById(id);
    entityManager.flush();

    assertThat(storeJpaRepository.findById(id)).isEmpty();
  }
}
