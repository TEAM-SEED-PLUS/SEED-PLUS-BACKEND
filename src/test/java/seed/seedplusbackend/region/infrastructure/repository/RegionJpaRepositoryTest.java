package seed.seedplusbackend.region.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import seed.seedplusbackend.region.domain.entity.Region;
import seed.seedplusbackend.region.domain.entity.RegionCodeType;
import seed.seedplusbackend.support.AbstractPostgresContainerTest;
import seed.seedplusbackend.support.RepositoryTest;
import seed.seedplusbackend.support.fixture.RegionFixture;

@RepositoryTest
@DisplayName("RegionJpaRepository")
class RegionJpaRepositoryTest extends AbstractPostgresContainerTest {

  @Autowired private RegionJpaRepository regionJpaRepository;
  @Autowired private EntityManager entityManager;

  @Test
  @DisplayName("지역을 저장하면 ID가 자동 부여된다")
  void save_persistsRegionAndAssignsId() {
    Region region = RegionFixture.seoulGangnamYeoksamLegalDong();

    Region saved = regionJpaRepository.save(region);

    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getCodeType()).isEqualTo(RegionCodeType.LEGAL_DONG);
  }

  @Test
  @DisplayName("ID로 지역을 조회하면 저장된 데이터가 반환된다")
  void findById_returnsSavedRegion() {
    Region saved = regionJpaRepository.save(RegionFixture.seoulGangnamYeoksamLegalDong());

    Region found = regionJpaRepository.findById(saved.getId()).orElseThrow();

    assertThat(found.getSido()).isEqualTo("서울특별시");
    assertThat(found.getDong()).isEqualTo("역삼동");
  }

  @Test
  @DisplayName("같은 code 값이 중복 저장되면 부분 유니크 인덱스에 의해 무결성 예외가 발생한다")
  void save_throwsDataIntegrityViolation_whenCodeDuplicates() {
    regionJpaRepository.save(RegionFixture.seoulGangnamYeoksamLegalDong());
    entityManager.flush();

    assertThatThrownBy(
            () -> {
              regionJpaRepository.save(RegionFixture.seoulGangnamYeoksamLegalDong());
              entityManager.flush();
            })
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  @DisplayName("code가 null인 지역은 여러 건 저장될 수 있다(부분 유니크 인덱스)")
  void save_allowsMultipleNullCodes_dueToPartialUniqueIndex() {
    Region first =
        Region.builder()
            .sido("서울특별시")
            .sigungu("강남구")
            .dong("논현동")
            .code(null)
            .codeType(RegionCodeType.ADMIN_DONG)
            .build();
    Region second =
        Region.builder()
            .sido("서울특별시")
            .sigungu("강남구")
            .dong("청담동")
            .code(null)
            .codeType(RegionCodeType.ADMIN_DONG)
            .build();

    regionJpaRepository.save(first);
    regionJpaRepository.save(second);
    entityManager.flush();

    assertThat(regionJpaRepository.count()).isGreaterThanOrEqualTo(2);
  }

  @Test
  @DisplayName("지역을 삭제하면 더 이상 조회되지 않는다")
  void deleteById_removesRegion() {
    Region saved = regionJpaRepository.save(RegionFixture.seoulGangnamYeoksamLegalDong());
    Long id = saved.getId();

    regionJpaRepository.deleteById(id);
    entityManager.flush();

    assertThat(regionJpaRepository.findById(id)).isEmpty();
  }
}
