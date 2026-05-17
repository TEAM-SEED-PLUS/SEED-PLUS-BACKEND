package seed.seedplusbackend.commercial.application;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seed.seedplusbackend.commercial.application.query.CommercialAreaSearchQuery;
import seed.seedplusbackend.commercial.application.result.CommercialAreaDetailResult;
import seed.seedplusbackend.commercial.application.result.CommercialAreaMetricResult;
import seed.seedplusbackend.commercial.application.result.CommercialAreaRegionResult;
import seed.seedplusbackend.commercial.application.result.CommercialAreaResult;
import seed.seedplusbackend.commercial.application.result.PagedCommercialAreaResult;
import seed.seedplusbackend.commercial.domain.entity.CommercialArea;
import seed.seedplusbackend.commercial.domain.entity.CommercialAreaRegionMapping;
import seed.seedplusbackend.commercial.domain.entity.CommercialAreaStatus;
import seed.seedplusbackend.commercial.domain.repository.CommercialAreaRegionMappingRepository;
import seed.seedplusbackend.commercial.domain.repository.CommercialAreaRepository;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.metrics.domain.entity.CommercialAreaMonthlyMetric;
import seed.seedplusbackend.metrics.domain.repository.CommercialAreaMonthlyMetricRepository;

@Service
@RequiredArgsConstructor
public class CommercialAreaQueryService {

  private final CommercialAreaRepository commercialAreaRepository;
  private final CommercialAreaRegionMappingRepository commercialAreaRegionMappingRepository;
  private final CommercialAreaMonthlyMetricRepository commercialAreaMonthlyMetricRepository;

  @Transactional(readOnly = true)
  public PagedCommercialAreaResult getCommercialAreas(CommercialAreaSearchQuery query) {
    Set<Long> commercialAreaIdsByRegion = getCommercialAreaIdsByRegion(query.regionId());

    List<CommercialAreaResult> filtered =
        commercialAreaRepository.findByStatusOrderByIdAsc(query.status()).stream()
            .filter(commercialArea -> matchesType(commercialArea, query))
            .filter(commercialArea -> matchesRegion(commercialArea, commercialAreaIdsByRegion))
            .map(CommercialAreaResult::from)
            .toList();

    return new PagedCommercialAreaResult(
        paginate(filtered, query.page(), query.size()),
        query.page(),
        query.size(),
        filtered.size());
  }

  @Transactional(readOnly = true)
  public CommercialAreaDetailResult getCommercialArea(Long commercialAreaId) {
    CommercialArea commercialArea = getVisibleCommercialArea(commercialAreaId);
    List<CommercialAreaRegionResult> regions = getRegionResults(commercialAreaId);
    CommercialAreaMetricResult latestMetric =
        commercialAreaMonthlyMetricRepository
            .findFirstByCommercialArea_IdOrderByReferenceMonthDesc(commercialAreaId)
            .map(CommercialAreaMetricResult::from)
            .orElse(null);

    return CommercialAreaDetailResult.from(commercialArea, regions, latestMetric);
  }

  @Transactional(readOnly = true)
  public List<CommercialAreaMetricResult> getCommercialAreaMetrics(
      Long commercialAreaId, LocalDate startMonth, LocalDate endMonth) {
    validatePeriod(startMonth, endMonth);
    getVisibleCommercialArea(commercialAreaId);

    return commercialAreaMonthlyMetricRepository
        .findByCommercialArea_IdOrderByReferenceMonthAsc(commercialAreaId)
        .stream()
        .filter(metric -> matchesPeriod(metric, startMonth, endMonth))
        .map(CommercialAreaMetricResult::from)
        .toList();
  }

  private CommercialArea getVisibleCommercialArea(Long commercialAreaId) {
    return commercialAreaRepository
        .findByIdAndStatusNot(commercialAreaId, CommercialAreaStatus.DELETED)
        .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND_COMMERCIAL_AREA));
  }

  private Set<Long> getCommercialAreaIdsByRegion(Long regionId) {
    if (regionId == null) {
      return null;
    }

    List<CommercialAreaRegionMapping> mappings =
        commercialAreaRegionMappingRepository.findByRegion_Id(regionId);
    Set<Long> commercialAreaIds = new HashSet<>();
    mappings.forEach(mapping -> commercialAreaIds.add(mapping.getCommercialArea().getId()));
    return commercialAreaIds;
  }

  private boolean matchesType(CommercialArea commercialArea, CommercialAreaSearchQuery query) {
    return query.type() == null || commercialArea.getType() == query.type();
  }

  private boolean matchesRegion(
      CommercialArea commercialArea, Set<Long> commercialAreaIdsByRegion) {
    return commercialAreaIdsByRegion == null
        || commercialAreaIdsByRegion.contains(commercialArea.getId());
  }

  private List<CommercialAreaResult> paginate(
      List<CommercialAreaResult> commercialAreas, int page, int size) {
    if (commercialAreas.isEmpty()) {
      return List.of();
    }

    int fromIndex = Math.min(page * size, commercialAreas.size());
    int toIndex = Math.min(fromIndex + size, commercialAreas.size());
    return commercialAreas.subList(fromIndex, toIndex);
  }

  private List<CommercialAreaRegionResult> getRegionResults(Long commercialAreaId) {
    return commercialAreaRegionMappingRepository.findByCommercialArea_Id(commercialAreaId).stream()
        .sorted(
            Comparator.comparing(CommercialAreaRegionMapping::isPrimary)
                .reversed()
                .thenComparing(CommercialAreaRegionMapping::getId))
        .map(CommercialAreaRegionResult::from)
        .toList();
  }

  private void validatePeriod(LocalDate startMonth, LocalDate endMonth) {
    if (startMonth != null && endMonth != null && startMonth.isAfter(endMonth)) {
      throw new ApplicationException(
          ErrorCode.INVALID_PARAMETER, "startMonth는 endMonth보다 이후일 수 없습니다.");
    }
  }

  private boolean matchesPeriod(
      CommercialAreaMonthlyMetric metric, LocalDate startMonth, LocalDate endMonth) {
    LocalDate referenceMonth = metric.getReferenceMonth();

    if (startMonth != null && referenceMonth.isBefore(startMonth)) {
      return false;
    }

    return endMonth == null || !referenceMonth.isAfter(endMonth);
  }
}
