package seed.seedplusbackend.industry.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.industry.application.result.IndustryHierarchyResult;
import seed.seedplusbackend.industry.domain.entity.Industry;
import seed.seedplusbackend.industry.domain.entity.IndustryLevel;
import seed.seedplusbackend.industry.domain.entity.IndustryStatus;
import seed.seedplusbackend.industry.domain.repository.IndustryRepository;

@Component
@RequiredArgsConstructor
public class IndustryHierarchyResolver {

  private final IndustryRepository industryRepository;

  @Transactional(readOnly = true)
  public IndustryHierarchyResult resolve(String industryCode) {
    Industry industry =
        industryRepository
            .findByIndustryCodeAndStatus(industryCode, IndustryStatus.ACTIVE)
            .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND_INDUSTRY));

    return switch (industry.getLevel()) {
      case LARGE -> new IndustryHierarchyResult(industry.getIndustryCode(), null, null);
      case MEDIUM -> {
        Industry large = requireParent(industry, IndustryLevel.LARGE);
        yield new IndustryHierarchyResult(
            large.getIndustryCode(), industry.getIndustryCode(), null);
      }
      case SMALL -> {
        Industry medium = requireParent(industry, IndustryLevel.MEDIUM);
        Industry large = requireParent(medium, IndustryLevel.LARGE);
        yield new IndustryHierarchyResult(
            large.getIndustryCode(), medium.getIndustryCode(), industry.getIndustryCode());
      }
    };
  }

  private Industry requireParent(Industry industry, IndustryLevel expectedLevel) {
    Industry parent = industry.getParentIndustry();
    if (parent == null || parent.getLevel() != expectedLevel) {
      throw new ApplicationException(
          ErrorCode.NOT_FOUND_INDUSTRY,
          "invalid hierarchy: industryCode=%s, expectedParentLevel=%s"
              .formatted(industry.getIndustryCode(), expectedLevel),
          null);
    }
    return parent;
  }
}
