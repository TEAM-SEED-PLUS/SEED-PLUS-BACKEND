package seed.seedplusbackend.industry.application;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.industry.application.result.IndustryResult;
import seed.seedplusbackend.industry.domain.entity.Industry;
import seed.seedplusbackend.industry.domain.entity.IndustryLevel;
import seed.seedplusbackend.industry.domain.entity.IndustryStatus;
import seed.seedplusbackend.industry.domain.repository.IndustryRepository;

@Service
@RequiredArgsConstructor
public class IndustryQueryService {

  private final IndustryRepository industryRepository;

  @Transactional(readOnly = true)
  public List<IndustryResult> getIndustries(IndustryLevel level, Long parentId) {
    List<Industry> industries = industryRepository.findByStatusOrderByIdAsc(IndustryStatus.ACTIVE);
    Map<Long, List<Industry>> childrenByParentId = groupByParentId(industries);

    return industries.stream()
        .filter(industry -> matchesLevel(industry, level))
        .filter(industry -> matchesParent(industry, parentId, level))
        .map(industry -> toResult(industry, childrenByParentId))
        .toList();
  }

  @Transactional(readOnly = true)
  public IndustryResult getIndustry(Long industryId) {
    Industry industry =
        industryRepository
            .findByIdAndStatus(industryId, IndustryStatus.ACTIVE)
            .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND_INDUSTRY));

    List<Industry> industries = industryRepository.findByStatusOrderByIdAsc(IndustryStatus.ACTIVE);
    return toResult(industry, groupByParentId(industries));
  }

  private Map<Long, List<Industry>> groupByParentId(List<Industry> industries) {
    return industries.stream()
        .filter(industry -> industry.getParentIndustry() != null)
        .collect(Collectors.groupingBy(industry -> industry.getParentIndustry().getId()));
  }

  private boolean matchesLevel(Industry industry, IndustryLevel level) {
    return level == null || industry.getLevel() == level;
  }

  private boolean matchesParent(Industry industry, Long parentId, IndustryLevel level) {
    if (parentId != null) {
      return industry.getParentIndustry() != null
          && Objects.equals(industry.getParentIndustry().getId(), parentId);
    }

    if (level != null) {
      return true;
    }

    return industry.getParentIndustry() == null;
  }

  private IndustryResult toResult(Industry industry, Map<Long, List<Industry>> childrenByParentId) {
    List<IndustryResult> children =
        childrenByParentId.getOrDefault(industry.getId(), List.of()).stream()
            .map(child -> toResult(child, childrenByParentId))
            .toList();

    return IndustryResult.from(industry, children);
  }
}
