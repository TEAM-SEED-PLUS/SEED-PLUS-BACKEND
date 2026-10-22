package seed.seedplusbackend.support.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import seed.seedplusbackend.support.domain.entity.Faq;
import seed.seedplusbackend.support.domain.entity.FaqCategory;
import seed.seedplusbackend.support.domain.entity.FaqGroup;

@DisplayName("FaqQueryService")
class FaqQueryServiceTest {

  private final FaqQueryService faqQueryService = new FaqQueryService(new FaqCatalog());

  @Test
  @DisplayName("사용자 그룹으로 FAQ를 조회한다")
  void getFaqs_filtersByGroup() {
    Page<Faq> result = faqQueryService.getFaqs(FaqGroup.PARTNER, null, 0, 20);

    assertThat(result.getTotalElements()).isEqualTo(5);
    assertThat(result.getContent()).allMatch(faq -> faq.group() == FaqGroup.PARTNER);
  }

  @Test
  @DisplayName("카테고리로 FAQ를 조회한다")
  void getFaqs_filtersByCategory() {
    Page<Faq> result = faqQueryService.getFaqs(null, FaqCategory.ANALYSIS, 0, 20);

    assertThat(result.getTotalElements()).isEqualTo(4);
    assertThat(result.getContent()).allMatch(faq -> faq.category() == FaqCategory.ANALYSIS);
  }

  @Test
  @DisplayName("FAQ 목록을 페이지 단위로 조회한다")
  void getFaqs_returnsRequestedPage() {
    Page<Faq> result = faqQueryService.getFaqs(FaqGroup.GENERAL, null, 1, 5);

    assertThat(result.getTotalElements()).isEqualTo(15);
    assertThat(result.getContent()).extracting(Faq::id).containsExactly(6L, 7L, 8L, 9L, 10L);
  }
}
