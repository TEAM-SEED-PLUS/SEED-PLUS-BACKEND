package seed.seedplusbackend.commercial.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import seed.seedplusbackend.commercial.domain.entity.ExternalDataSource;
import seed.seedplusbackend.commercial.domain.entity.RegionExternalCodeMapping;
import seed.seedplusbackend.support.AbstractPostgresContainerTest;
import seed.seedplusbackend.support.RepositoryTest;

@RepositoryTest
@DisplayName("지역 외부 코드 매핑 Repository")
class RegionExternalCodeMappingJpaRepositoryTest extends AbstractPostgresContainerTest {

  private static final String TEST_REGION_CODE = "9999999999";

  @Autowired private RegionExternalCodeMappingJpaRepository mappingRepository;

  @Test
  @DisplayName("지역과 데이터 출처에 연결된 외부 코드를 조회한다")
  void savesAndFindsExternalCodes() {
    RegionExternalCodeMapping first = mappingRepository.save(mapping("10117", "역삼역2번 출구"));
    RegionExternalCodeMapping second = mappingRepository.save(mapping("10118", "역삼역7번 출구"));

    assertThat(
            mappingRepository.findAllByRegionCodeAndSource(
                TEST_REGION_CODE, ExternalDataSource.SMALL_BUSINESS_STORE))
        .containsExactlyInAnyOrder(first, second);
  }

  @Test
  @DisplayName("Flyway가 역삼동의 소상공인 주요상권 코드를 적재한다")
  void flywaySeedsYeoksamExternalCodes() {
    assertThat(
            mappingRepository
                .findAllByRegionCodeAndSource("1168010100", ExternalDataSource.SMALL_BUSINESS_STORE)
                .stream()
                .map(RegionExternalCodeMapping::getExternalCode))
        .containsExactlyInAnyOrder("10116", "10117", "10118", "10132", "10133", "10134");
  }

  private RegionExternalCodeMapping mapping(String externalCode, String externalName) {
    return RegionExternalCodeMapping.builder()
        .regionCode(TEST_REGION_CODE)
        .source(ExternalDataSource.SMALL_BUSINESS_STORE)
        .externalCode(externalCode)
        .externalName(externalName)
        .build();
  }
}
