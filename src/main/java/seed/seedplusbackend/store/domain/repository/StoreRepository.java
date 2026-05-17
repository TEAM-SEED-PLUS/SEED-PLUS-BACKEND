package seed.seedplusbackend.store.domain.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import seed.seedplusbackend.store.domain.entity.Store;
import seed.seedplusbackend.store.domain.entity.StoreStatus;

public interface StoreRepository {

  <S extends Store> S save(S entity);

  Optional<Store> findById(Long id);

  Optional<Store> findByIdAndStatus(Long id, StoreStatus status);

  List<Store> findAll();

  Page<Store> searchStores(
      StoreStatus status,
      Long buildingId,
      Long industryId,
      Long regionId,
      Boolean isVacant,
      Pageable pageable);

  long countByBuilding_Id(Long buildingId);

  long countByBuilding_IdAndVacantTrue(Long buildingId);

  boolean existsById(Long id);

  boolean existsByIdAndStatus(Long id, StoreStatus status);

  void delete(Store entity);

  void deleteById(Long id);

  long count();
}
