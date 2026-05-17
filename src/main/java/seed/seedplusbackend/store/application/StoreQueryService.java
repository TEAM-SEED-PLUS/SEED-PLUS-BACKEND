package seed.seedplusbackend.store.application;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.metrics.domain.entity.StoreFinancialMonthlyMetric;
import seed.seedplusbackend.metrics.domain.entity.StoreOperationMonthlyMetric;
import seed.seedplusbackend.metrics.domain.repository.StoreFinancialMonthlyMetricRepository;
import seed.seedplusbackend.metrics.domain.repository.StoreOperationMonthlyMetricRepository;
import seed.seedplusbackend.score.domain.entity.ScoreTargetType;
import seed.seedplusbackend.score.domain.repository.ScoreSnapshotRepository;
import seed.seedplusbackend.store.domain.entity.Store;
import seed.seedplusbackend.store.domain.entity.StoreStatus;
import seed.seedplusbackend.store.domain.repository.StoreRepository;

@Service
@RequiredArgsConstructor
public class StoreQueryService {

  private static final int MAX_PAGE_SIZE = 100;

  private final StoreRepository storeRepository;
  private final StoreOperationMonthlyMetricRepository storeOperationMonthlyMetricRepository;
  private final StoreFinancialMonthlyMetricRepository storeFinancialMonthlyMetricRepository;
  private final ScoreSnapshotRepository scoreSnapshotRepository;

  @Transactional(readOnly = true)
  public Page<Store> getStores(
      int page, int size, Long buildingId, Long industryId, Long regionId, Boolean isVacant) {
    validatePage(page, size);

    return storeRepository.searchStores(
        StoreStatus.ACTIVE,
        buildingId,
        industryId,
        regionId,
        isVacant,
        PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id")));
  }

  @Transactional(readOnly = true)
  public StoreDetailResult getStore(Long storeId) {
    Store store = getActiveStore(storeId);

    return new StoreDetailResult(
        store,
        scoreSnapshotRepository
            .findFirstByStoreIdAndTargetTypeOrderByReferenceMonthDescIdDesc(
                storeId, ScoreTargetType.STORE)
            .orElse(null));
  }

  @Transactional(readOnly = true)
  public List<StoreOperationMonthlyMetric> getStoreOperationMetrics(
      Long storeId, LocalDate startMonth, LocalDate endMonth) {
    validateDateRange(startMonth, endMonth);
    validateStoreExists(storeId);

    return storeOperationMonthlyMetricRepository.findByStoreIdAndReferenceMonthRange(
        storeId, startMonth, endMonth);
  }

  @Transactional(readOnly = true)
  public List<StoreFinancialMonthlyMetric> getStoreFinancialMetrics(
      Long storeId, LocalDate startMonth, LocalDate endMonth) {
    validateDateRange(startMonth, endMonth);
    validateStoreExists(storeId);

    return storeFinancialMonthlyMetricRepository.findByStoreIdAndReferenceMonthRange(
        storeId, startMonth, endMonth);
  }

  private Store getActiveStore(Long storeId) {
    return storeRepository
        .findByIdAndStatus(storeId, StoreStatus.ACTIVE)
        .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND_STORE));
  }

  private void validateStoreExists(Long storeId) {
    if (!storeRepository.existsByIdAndStatus(storeId, StoreStatus.ACTIVE)) {
      throw new ApplicationException(ErrorCode.NOT_FOUND_STORE);
    }
  }

  private void validatePage(int page, int size) {
    if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
      throw new ApplicationException(ErrorCode.INVALID_PARAMETER);
    }
  }

  private void validateDateRange(LocalDate startMonth, LocalDate endMonth) {
    if (startMonth != null && endMonth != null && startMonth.isAfter(endMonth)) {
      throw new ApplicationException(ErrorCode.INVALID_PARAMETER);
    }
  }
}
