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
import seed.seedplusbackend.commercial.domain.entity.ExternalDataSource;
import seed.seedplusbackend.commercial.domain.entity.RegionExternalCodeMapping;
import seed.seedplusbackend.commercial.domain.repository.RegionExternalCodeMappingRepository;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.region.application.RegionResolver;
import seed.seedplusbackend.region.domain.entity.Region;

@ExtendWith(MockitoExtension.class)
@DisplayName("지역 외부 코드 조회기")
class RegionExternalCodeResolverTest {

  private static final String REGION_CODE = "1168010100";
  private static final ExternalDataSource SOURCE = ExternalDataSource.SMALL_BUSINESS_STORE;

  @Mock private RegionResolver regionResolver;
  @Mock private RegionExternalCodeMappingRepository externalCodeMappingRepository;

  @Test
  @DisplayName("법정동에 연결된 외부 코드를 중복 없이 조회한다")
  void resolvesDistinctExternalCodes() {
    Region region = mock(Region.class);
    RegionExternalCodeMapping first = mapping("10118");
    RegionExternalCodeMapping second = mapping("10117");
    RegionExternalCodeMapping duplicated = mapping("10117");
    given(region.getId()).willReturn(1L);
    given(regionResolver.resolveLegalDong(REGION_CODE)).willReturn(region);
    given(externalCodeMappingRepository.findAllByRegionIdAndSource(1L, SOURCE))
        .willReturn(List.of(first, second, duplicated));

    assertThat(resolver().resolve(REGION_CODE, SOURCE)).containsExactly("10117", "10118");
  }

  @Test
  @DisplayName("법정동에 연결된 외부 코드가 없으면 실패한다")
  void throwsWhenExternalCodeIsNotMapped() {
    Region region = mock(Region.class);
    given(region.getId()).willReturn(1L);
    given(regionResolver.resolveLegalDong(REGION_CODE)).willReturn(region);
    given(externalCodeMappingRepository.findAllByRegionIdAndSource(1L, SOURCE))
        .willReturn(List.of());

    assertThatThrownBy(() -> resolver().resolve(REGION_CODE, SOURCE))
        .isInstanceOf(ApplicationException.class)
        .hasMessageContaining(REGION_CODE)
        .hasMessageContaining(SOURCE.name())
        .extracting("errorCode")
        .isEqualTo(ErrorCode.NOT_FOUND_COMMERCIAL_AREA);
  }

  private RegionExternalCodeResolver resolver() {
    return new RegionExternalCodeResolver(regionResolver, externalCodeMappingRepository);
  }

  private RegionExternalCodeMapping mapping(String externalCode) {
    RegionExternalCodeMapping mapping = mock(RegionExternalCodeMapping.class);
    given(mapping.getExternalCode()).willReturn(externalCode);
    return mapping;
  }
}
