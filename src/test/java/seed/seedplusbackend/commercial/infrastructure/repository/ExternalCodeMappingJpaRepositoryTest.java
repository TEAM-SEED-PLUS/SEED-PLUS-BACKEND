package seed.seedplusbackend.commercial.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import seed.seedplusbackend.commercial.domain.entity.CommercialArea;
import seed.seedplusbackend.commercial.domain.entity.CommercialAreaExternalCodeMapping;
import seed.seedplusbackend.commercial.domain.entity.ExternalDataSource;
import seed.seedplusbackend.commercial.domain.entity.IndustryExternalCodeMapping;
import seed.seedplusbackend.industry.domain.entity.Industry;
import seed.seedplusbackend.industry.infrastructure.repository.IndustryJpaRepository;
import seed.seedplusbackend.support.AbstractPostgresContainerTest;
import seed.seedplusbackend.support.RepositoryTest;
import seed.seedplusbackend.support.fixture.CommercialAreaFixture;
import seed.seedplusbackend.support.fixture.IndustryFixture;

@RepositoryTest
@DisplayName("외부 공공데이터 코드 매핑 Repository")
class ExternalCodeMappingJpaRepositoryTest extends AbstractPostgresContainerTest {

  @Autowired private CommercialAreaJpaRepository commercialAreaRepository;
  @Autowired private IndustryJpaRepository industryRepository;
  @Autowired private CommercialAreaExternalCodeMappingJpaRepository commercialAreaMappingRepository;
  @Autowired private IndustryExternalCodeMappingJpaRepository industryMappingRepository;

  @Test
  @DisplayName("내부 상권과 업종에 연결된 외부 코드를 조회한다")
  void savesAndFindsExternalCodes() {
    CommercialArea commercialArea =
        commercialAreaRepository.save(CommercialAreaFixture.developedActive("테스트 상권"));
    Industry industry = industryRepository.save(IndustryFixture.largeRoot("TEST-EXT", "테스트 업종"));
    CommercialAreaExternalCodeMapping areaMapping =
        commercialAreaMappingRepository.save(
            CommercialAreaExternalCodeMapping.builder()
                .commercialArea(commercialArea)
                .source(ExternalDataSource.SMALL_BUSINESS_STORE)
                .externalCode("9151")
                .externalName("테스트 상권")
                .build());
    IndustryExternalCodeMapping industryMapping =
        industryMappingRepository.save(
            IndustryExternalCodeMapping.builder()
                .industry(industry)
                .source(ExternalDataSource.KOSIS_BUSINESS_COUNT)
                .externalCode("I")
                .externalName("숙박 및 음식점업")
                .build());

    assertThat(
            commercialAreaMappingRepository.findAllByCommercialAreaIdAndSource(
                commercialArea.getId(), ExternalDataSource.SMALL_BUSINESS_STORE))
        .containsExactly(areaMapping);
    assertThat(
            industryMappingRepository.findAllByIndustryIdAndSource(
                industry.getId(), ExternalDataSource.KOSIS_BUSINESS_COUNT))
        .containsExactly(industryMapping);
  }
}
