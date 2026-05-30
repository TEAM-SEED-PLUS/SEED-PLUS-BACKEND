package seed.seedplusbackend.industry.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import seed.seedplusbackend.industry.domain.entity.Industry;
import seed.seedplusbackend.support.AbstractPostgresContainerTest;
import seed.seedplusbackend.support.RepositoryTest;
import seed.seedplusbackend.support.fixture.IndustryFixture;

@RepositoryTest
@DisplayName("IndustryJpaRepository")
class IndustryJpaRepositoryTest extends AbstractPostgresContainerTest {

  @Autowired private IndustryJpaRepository industryJpaRepository;
  @Autowired private EntityManager entityManager;

  @Test
  @DisplayName("업종을 저장하면 ID가 자동 부여된다")
  void save_persistsIndustryAndAssignsId() {
    Industry industry = IndustryFixture.largeRoot("L01", "음식점업");

    Industry saved = industryJpaRepository.save(industry);

    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getIndustryCode()).isEqualTo("L01");
  }

  @Test
  @DisplayName("자기참조 부모 업종을 가진 자식 업종이 저장되고 부모 식별자가 보존된다")
  void save_persistsChildWithParentReference() {
    Industry parent = industryJpaRepository.save(IndustryFixture.largeRoot("L02", "도소매업"));
    entityManager.flush();

    Industry child =
        industryJpaRepository.save(IndustryFixture.mediumChild("M02-1", "식료품 도매", parent));
    entityManager.flush();
    entityManager.clear();

    Industry refreshed = industryJpaRepository.findById(child.getId()).orElseThrow();
    assertThat(refreshed.getParentIndustry()).isNotNull();
    assertThat(refreshed.getParentIndustry().getId()).isEqualTo(parent.getId());
  }

  @Test
  @DisplayName("같은 industry_code를 중복 저장하면 무결성 예외가 발생한다")
  void save_throwsDataIntegrityViolation_whenIndustryCodeDuplicates() {
    industryJpaRepository.save(IndustryFixture.largeRoot("L03", "건설업"));
    entityManager.flush();

    assertThatThrownBy(
            () -> {
              industryJpaRepository.save(IndustryFixture.largeRoot("L03", "다른이름"));
              entityManager.flush();
            })
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  @DisplayName("findAll은 저장된 모든 업종을 반환한다")
  void findAll_returnsAllIndustries() {
    industryJpaRepository.save(IndustryFixture.largeRoot("L04", "교육업"));
    industryJpaRepository.save(IndustryFixture.largeRoot("L05", "의료업"));

    long count = industryJpaRepository.count();

    assertThat(count).isGreaterThanOrEqualTo(2);
  }

  @Test
  @DisplayName("업종을 삭제하면 더 이상 조회되지 않는다")
  void deleteById_removesIndustry() {
    Industry saved = industryJpaRepository.save(IndustryFixture.largeRoot("L99", "삭제대상"));
    Long id = saved.getId();

    industryJpaRepository.deleteById(id);
    entityManager.flush();

    assertThat(industryJpaRepository.findById(id)).isEmpty();
  }
}
