package seed.seedplusbackend.industry.application;

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
import seed.seedplusbackend.industry.application.result.IndustryHierarchyResult;
import seed.seedplusbackend.industry.domain.entity.Industry;
import seed.seedplusbackend.industry.domain.entity.IndustryLevel;
import seed.seedplusbackend.industry.domain.entity.IndustryStatus;
import seed.seedplusbackend.industry.domain.repository.IndustryRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("업종 계층 조회기")
class IndustryHierarchyResolverTest {

  @Mock private IndustryRepository industryRepository;

  @Test
  @DisplayName("소분류 업종 코드로 대분류부터 소분류까지 조회한다")
  void resolvesSmallIndustryHierarchy() {
    Industry large = industry("G2", IndustryLevel.LARGE, null);
    Industry medium = industry("G221", IndustryLevel.MEDIUM, large);
    Industry small = industry("G22199", IndustryLevel.SMALL, medium);
    given(industryRepository.findByIndustryCodeAndStatus("G22199", IndustryStatus.ACTIVE))
        .willReturn(Optional.of(small));

    IndustryHierarchyResult result = resolver().resolve("G22199");

    assertThat(result.largeIndustryCode()).isEqualTo("G2");
    assertThat(result.mediumIndustryCode()).isEqualTo("G221");
    assertThat(result.smallIndustryCode()).isEqualTo("G22199");
  }

  @Test
  @DisplayName("중분류 업종이면 소분류 코드는 비워둔다")
  void resolvesMediumIndustryHierarchy() {
    Industry large = industry("G2", IndustryLevel.LARGE, null);
    Industry medium = industry("G221", IndustryLevel.MEDIUM, large);
    given(industryRepository.findByIndustryCodeAndStatus("G221", IndustryStatus.ACTIVE))
        .willReturn(Optional.of(medium));

    assertThat(resolver().resolve("G221"))
        .isEqualTo(new IndustryHierarchyResult("G2", "G221", null));
  }

  @Test
  @DisplayName("대분류 업종이면 하위 분류 코드는 비워둔다")
  void resolvesLargeIndustryHierarchy() {
    Industry large = industry("G2", IndustryLevel.LARGE, null);
    given(industryRepository.findByIndustryCodeAndStatus("G2", IndustryStatus.ACTIVE))
        .willReturn(Optional.of(large));

    assertThat(resolver().resolve("G2")).isEqualTo(new IndustryHierarchyResult("G2", null, null));
  }

  @Test
  @DisplayName("활성 업종 코드가 없으면 실패한다")
  void throwsWhenIndustryDoesNotExist() {
    given(industryRepository.findByIndustryCodeAndStatus("UNKNOWN", IndustryStatus.ACTIVE))
        .willReturn(Optional.empty());

    assertThatThrownBy(() -> resolver().resolve("UNKNOWN"))
        .isInstanceOf(ApplicationException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.NOT_FOUND_INDUSTRY);
  }

  @Test
  @DisplayName("부모 업종 계층이 올바르지 않으면 잘못된 코드를 만들지 않고 실패한다")
  void throwsWhenParentHierarchyIsInvalid() {
    Industry invalidParent = industry("G221", IndustryLevel.MEDIUM, null);
    Industry small = industry("G22199", IndustryLevel.SMALL, invalidParent);
    given(industryRepository.findByIndustryCodeAndStatus("G22199", IndustryStatus.ACTIVE))
        .willReturn(Optional.of(small));

    assertThatThrownBy(() -> resolver().resolve("G22199"))
        .isInstanceOf(ApplicationException.class)
        .hasMessageContaining("invalid hierarchy")
        .extracting("errorCode")
        .isEqualTo(ErrorCode.NOT_FOUND_INDUSTRY);
  }

  private IndustryHierarchyResolver resolver() {
    return new IndustryHierarchyResolver(industryRepository);
  }

  private Industry industry(String code, IndustryLevel level, Industry parent) {
    return Industry.builder()
        .industryCode(code)
        .name(code)
        .parentIndustry(parent)
        .level(level)
        .status(IndustryStatus.ACTIVE)
        .build();
  }
}
