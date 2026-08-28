package seed.seedplusbackend.builderstore.application.port;

import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreBookmarkSnapshot;

public interface BuilderStoreBookmarkSnapshotResolver {

  BuilderStoreBookmarkSnapshot resolve(
      String regionCode, String industryCode, Long regionId, Long commercialAreaId);
}
