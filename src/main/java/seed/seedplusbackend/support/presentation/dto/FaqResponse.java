package seed.seedplusbackend.support.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import seed.seedplusbackend.support.domain.entity.Faq;
import seed.seedplusbackend.support.domain.entity.FaqCategory;
import seed.seedplusbackend.support.domain.entity.FaqGroup;

@Schema(description = "FAQ 항목")
public record FaqResponse(
    Long id,
    FaqGroup group,
    FaqCategory category,
    String question,
    String answer,
    int displayOrder) {

  public static FaqResponse from(Faq faq) {
    return new FaqResponse(
        faq.id(), faq.group(), faq.category(), faq.question(), faq.answer(), faq.displayOrder());
  }
}
