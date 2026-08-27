package seed.seedplusbackend.commercial.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import seed.seedplusbackend.commercial.domain.entity.ExternalDataSource;
import seed.seedplusbackend.commercial.domain.entity.RegionExternalCodeMapping;
import seed.seedplusbackend.region.domain.entity.Region;
import seed.seedplusbackend.region.infrastructure.repository.RegionJpaRepository;
import seed.seedplusbackend.support.AbstractPostgresContainerTest;
import seed.seedplusbackend.support.RepositoryTest;
import seed.seedplusbackend.support.fixture.RegionFixture;

@RepositoryTest
@DisplayName("지역 외부 코드 매핑 Repository")
class RegionExternalCodeMappingJpaRepositoryTest extends AbstractPostgresContainerTest {

  @Autowired private RegionJpaRepository regionRepository;
  @Autowired private RegionExternalCodeMappingJpaRepository mappingRepository;

  @Test
  @DisplayName("지역과 데이터 출처에 연결된 외부 코드를 조회한다")
  void savesAndFindsExternalCodes() {
    Region region = regionRepository.save(RegionFixture.seoulGangnamYeoksamLegalDong());
    RegionExternalCodeMapping first = mappingRepository.save(mapping(region, "10117", "역삼역2번 출구"));
    RegionExternalCodeMapping second = mappingRepository.save(mapping(region, "10118", "역삼역7번 출구"));

    assertThat(
            mappingRepository.findAllByRegionIdAndSource(
                region.getId(), ExternalDataSource.SMALL_BUSINESS_STORE))
        .containsExactlyInAnyOrder(first, second);
  }

  private RegionExternalCodeMapping mapping(
      Region region, String externalCode, String externalName) {
    return RegionExternalCodeMapping.builder()
        .region(region)
        .source(ExternalDataSource.SMALL_BUSINESS_STORE)
        .externalCode(externalCode)
        .externalName(externalName)
        .build();
  }
}
