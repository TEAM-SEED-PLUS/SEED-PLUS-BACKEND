package seed.seedplusbackend.commercial.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import seed.seedplusbackend.commercial.domain.entity.CommercialAreaExternalCodeMapping;
import seed.seedplusbackend.commercial.domain.entity.ExternalDataSource;
import seed.seedplusbackend.commercial.domain.repository.CommercialAreaExternalCodeMappingRepository;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.region.application.RegionResolver;
import seed.seedplusbackend.region.domain.entity.Region;

@ExtendWith(MockitoExtension.class)
@DisplayName("외부 상권 코드 조회기")
class CommercialAreaExternalCodeResolverTest {

  private static final String REGION_CODE = "1168010100";
  private static final ExternalDataSource SOURCE = ExternalDataSource.SMALL_BUSINESS_STORE;

  @Mock private RegionResolver regionResolver;
  @Mock private CommercialAreaExternalCodeMappingRepository externalCodeMappingRepository;

  @Test
  @DisplayName("법정동에 연결된 외부 상권 코드를 중복 없이 조회한다")
  void resolvesDistinctExternalCodes() {
    Region region = mock(Region.class);
    CommercialAreaExternalCodeMapping first = mapping("10117");
    CommercialAreaExternalCodeMapping duplicated = mapping("10117");
    CommercialAreaExternalCodeMapping second = mapping("10118");
    given(region.getId()).willReturn(1L);
    given(regionResolver.resolveLegalDong(REGION_CODE)).willReturn(region);
    given(externalCodeMappingRepository.findAllByRegionIdAndSource(1L, SOURCE))
        .willReturn(List.of(second, first, duplicated));

    CommercialAreaExternalCodeResolver resolver = resolver();

    assertThat(resolver.resolve(REGION_CODE, SOURCE)).containsExactly("10117", "10118");
  }

  @Test
  @DisplayName("법정동에 연결된 외부 상권 코드가 없으면 명확하게 실패한다")
  void throwsWhenExternalCodeIsNotMapped() {
    Region region = mock(Region.class);
    given(region.getId()).willReturn(1L);
    given(regionResolver.resolveLegalDong(REGION_CODE)).willReturn(region);
    given(externalCodeMappingRepository.findAllByRegionIdAndSource(1L, SOURCE))
        .willReturn(List.of());

    CommercialAreaExternalCodeResolver resolver = resolver();

    assertThatThrownBy(() -> resolver.resolve(REGION_CODE, SOURCE))
        .isInstanceOf(ApplicationException.class)
        .hasMessageContaining(REGION_CODE)
        .hasMessageContaining(SOURCE.name())
        .extracting("errorCode")
        .isEqualTo(ErrorCode.NOT_FOUND_COMMERCIAL_AREA);
  }

  private CommercialAreaExternalCodeResolver resolver() {
    return new CommercialAreaExternalCodeResolver(regionResolver, externalCodeMappingRepository);
  }

  private CommercialAreaExternalCodeMapping mapping(String externalCode) {
    CommercialAreaExternalCodeMapping mapping = mock(CommercialAreaExternalCodeMapping.class);
    given(mapping.getExternalCode()).willReturn(externalCode);
    return mapping;
  }
}
