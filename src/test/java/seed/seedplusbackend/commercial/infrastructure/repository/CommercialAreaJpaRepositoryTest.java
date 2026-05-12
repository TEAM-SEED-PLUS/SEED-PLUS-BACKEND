package seed.seedplusbackend.commercial.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import seed.seedplusbackend.commercial.domain.entity.CommercialArea;
import seed.seedplusbackend.commercial.domain.entity.CommercialAreaStatus;
import seed.seedplusbackend.commercial.domain.entity.CommercialAreaType;
import seed.seedplusbackend.support.AbstractPostgresContainerTest;
import seed.seedplusbackend.support.RepositoryTest;
import seed.seedplusbackend.support.fixture.CommercialAreaFixture;

@RepositoryTest
@DisplayName("CommercialAreaJpaRepository")
class CommercialAreaJpaRepositoryTest extends AbstractPostgresContainerTest {

  @Autowired private CommercialAreaJpaRepository commercialAreaJpaRepository;
  @Autowired private EntityManager entityManager;

  @Test
  @DisplayName("상권을 저장하면 ID가 자동 부여된다")
  void save_persistsCommercialAreaAndAssignsId() {
    CommercialArea area = CommercialAreaFixture.developedActive("강남역상권");

    CommercialArea saved = commercialAreaJpaRepository.save(area);

    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getType()).isEqualTo(CommercialAreaType.DEVELOPED);
    assertThat(saved.getStatus()).isEqualTo(CommercialAreaStatus.ACTIVE);
  }

  @Test
  @DisplayName("ID로 상권을 조회하면 저장된 데이터가 반환된다")
  void findById_returnsSavedCommercialArea() {
    CommercialArea saved =
        commercialAreaJpaRepository.save(CommercialAreaFixture.developedActive("선릉역상권"));

    CommercialArea found = commercialAreaJpaRepository.findById(saved.getId()).orElseThrow();

    assertThat(found.getName()).isEqualTo("선릉역상권");
  }

  @Test
  @DisplayName("findAll은 저장된 모든 상권을 반환한다")
  void findAll_returnsAllCommercialAreas() {
    commercialAreaJpaRepository.save(CommercialAreaFixture.developedActive("A상권"));
    commercialAreaJpaRepository.save(CommercialAreaFixture.developedActive("B상권"));

    long count = commercialAreaJpaRepository.count();

    assertThat(count).isGreaterThanOrEqualTo(2);
  }

  @Test
  @DisplayName("ALLEY 타입과 DEVELOPED 타입을 모두 저장할 수 있다")
  void save_acceptsAllValidTypeEnumValues() {
    CommercialArea alley =
        CommercialArea.builder()
            .name("골목상권")
            .type(CommercialAreaType.ALLEY)
            .description(null)
            .status(CommercialAreaStatus.ACTIVE)
            .build();
    CommercialArea tourism =
        CommercialArea.builder()
            .name("관광특구")
            .type(CommercialAreaType.TOURISM_SPECIAL_ZONE)
            .description(null)
            .status(CommercialAreaStatus.ACTIVE)
            .build();

    CommercialArea savedAlley = commercialAreaJpaRepository.save(alley);
    CommercialArea savedTourism = commercialAreaJpaRepository.save(tourism);
    entityManager.flush();

    assertThat(savedAlley.getType()).isEqualTo(CommercialAreaType.ALLEY);
    assertThat(savedTourism.getType()).isEqualTo(CommercialAreaType.TOURISM_SPECIAL_ZONE);
  }

  @Test
  @DisplayName("상권을 삭제하면 더 이상 조회되지 않는다")
  void deleteById_removesCommercialArea() {
    CommercialArea saved =
        commercialAreaJpaRepository.save(CommercialAreaFixture.developedActive("삭제대상상권"));
    Long id = saved.getId();

    commercialAreaJpaRepository.deleteById(id);
    entityManager.flush();

    assertThat(commercialAreaJpaRepository.findById(id)).isEmpty();
  }
}
