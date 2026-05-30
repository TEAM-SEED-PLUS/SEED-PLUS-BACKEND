package seed.seedplusbackend.industry.application.result;

import java.util.List;
import seed.seedplusbackend.industry.domain.entity.Industry;
import seed.seedplusbackend.industry.domain.entity.IndustryLevel;

public record IndustryResult(
    Long industryId,
    String industryCode,
    String name,
    Long parentIndustryId,
    IndustryLevel level,
    List<IndustryResult> children) {

  public IndustryResult {
    children = children == null ? List.of() : List.copyOf(children);
  }

  public static IndustryResult from(Industry industry, List<IndustryResult> children) {
    Long parentIndustryId =
        industry.getParentIndustry() == null ? null : industry.getParentIndustry().getId();

    return new IndustryResult(
        industry.getId(),
        industry.getIndustryCode(),
        industry.getName(),
        parentIndustryId,
        industry.getLevel(),
        children);
  }
}
