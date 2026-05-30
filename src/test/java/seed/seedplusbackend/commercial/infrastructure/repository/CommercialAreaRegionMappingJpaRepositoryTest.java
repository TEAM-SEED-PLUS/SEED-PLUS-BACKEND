package seed.seedplusbackend.commercial.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import seed.seedplusbackend.commercial.domain.entity.CommercialArea;
import seed.seedplusbackend.commercial.domain.entity.CommercialAreaRegionMapping;
import seed.seedplusbackend.region.domain.entity.Region;
import seed.seedplusbackend.region.infrastructure.repository.RegionJpaRepository;
import seed.seedplusbackend.support.AbstractPostgresContainerTest;
import seed.seedplusbackend.support.RepositoryTest;
import seed.seedplusbackend.support.fixture.CommercialAreaFixture;
import seed.seedplusbackend.support.fixture.RegionFixture;

@RepositoryTest
@DisplayName("CommercialAreaRegionMappingJpaRepository")
class CommercialAreaRegionMappingJpaRepositoryTest extends AbstractPostgresContainerTest {

  @Autowired private CommercialAreaRegionMappingJpaRepository mappingJpaRepository;
  @Autowired private CommercialAreaJpaRepository commercialAreaJpaRepository;
  @Autowired private RegionJpaRepository regionJpaRepository;

  @Test
  @DisplayName("상권-지역 매핑을 저장하고 조회할 수 있다")
  void saveAndFindById_smokeTest() {
    Region region = regionJpaRepository.save(RegionFixture.seoulGangnamYeoksamLegalDong());
    CommercialArea area =
        commercialAreaJpaRepository.save(CommercialAreaFixture.developedActive("매핑상권"));

    CommercialAreaRegionMapping mapping =
        CommercialAreaRegionMapping.builder()
            .commercialArea(area)
            .region(region)
            .primary(true)
            .build();
    CommercialAreaRegionMapping saved = mappingJpaRepository.save(mapping);

    assertThat(saved.getId()).isNotNull();
    assertThat(mappingJpaRepository.findById(saved.getId())).isPresent();
  }
}
