package seed.seedplusbackend.region.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.region.domain.entity.Region;
import seed.seedplusbackend.region.domain.entity.RegionCodeType;
import seed.seedplusbackend.region.domain.repository.RegionRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegionResolver")
class RegionResolverTest {

  @Mock private RegionRepository regionRepository;

  @Test
  @DisplayName("Resolves legal dong code to joined region name")
  void resolveLegalDongName_returnsJoinedRegionName() {
    RegionResolver resolver = new RegionResolver(regionRepository);
    given(regionRepository.findByCodeAndCodeType("1168010100", RegionCodeType.LEGAL_DONG))
        .willReturn(Optional.of(region()));

    String regionName = resolver.resolveLegalDongName("1168010100");

    assertThat(regionName).isEqualTo("Seoul Gangnam-gu Yeoksam-dong");
  }

  @Test
  @DisplayName("Throws NOT_FOUND_REGION when legal dong code is not found")
  void resolveLegalDong_throwsException_whenLegalDongNotFound() {
    RegionResolver resolver = new RegionResolver(regionRepository);
    given(regionRepository.findByCodeAndCodeType("1168010100", RegionCodeType.LEGAL_DONG))
        .willReturn(Optional.empty());

    assertThatThrownBy(() -> resolver.resolveLegalDong("1168010100"))
        .isInstanceOf(ApplicationException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.NOT_FOUND_REGION);
  }

  private Region region() {
    return Region.builder()
        .sido("Seoul")
        .sigungu("Gangnam-gu")
        .dong("Yeoksam-dong")
        .code("1168010100")
        .codeType(RegionCodeType.LEGAL_DONG)
        .build();
  }
}
