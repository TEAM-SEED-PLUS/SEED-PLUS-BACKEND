package seed.seedplusbackend.builderstore.application.result;

import java.util.Objects;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreBookmark;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreBookmarkSnapshot;

public record BuilderStoreBookmarkResult(
    BuilderStoreBookmark bookmark,
    BuilderStoreBookmarkSnapshot currentSnapshot,
    boolean estimatedSalesUpdateAvailable,
    boolean otherDataUpdateAvailable,
    Long collectionRunId) {

  public static BuilderStoreBookmarkResult of(
      BuilderStoreBookmark bookmark,
      BuilderStoreBookmarkSnapshot currentSnapshot,
      Long collectionRunId) {
    boolean salesChanged =
        changed(bookmark.getEstimatedSalesQuarter(), currentSnapshot.estimatedSalesQuarter());
    boolean otherChanged =
        changed(bookmark.getBusinessSurvivalYear(), currentSnapshot.businessSurvivalYear())
            || changed(bookmark.getBusinessCountYear(), currentSnapshot.businessCountYear())
            || changed(bookmark.getStoreInfoCollectedAt(), currentSnapshot.storeInfoCollectedAt())
            || changedRentPeriod(bookmark, currentSnapshot);
    return new BuilderStoreBookmarkResult(
        bookmark, currentSnapshot, salesChanged, otherChanged, collectionRunId);
  }

  private static boolean changed(Object saved, Object current) {
    return current != null && !Objects.equals(saved, current);
  }

  private static boolean changedRentPeriod(
      BuilderStoreBookmark bookmark, BuilderStoreBookmarkSnapshot current) {
    if (current.rentReferenceYear() == null || current.rentReferenceQuarter() == null) {
      return false;
    }
    return !Objects.equals(bookmark.getRentReferenceYear(), current.rentReferenceYear())
        || !Objects.equals(bookmark.getRentReferenceQuarter(), current.rentReferenceQuarter());
  }
}
