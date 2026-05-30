package seed.seedplusbackend.builderstore.application.result;

import java.util.List;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStore;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreImage;
import seed.seedplusbackend.score.domain.entity.ScoreSnapshot;

public record BuilderStoreDetailResult(
    BuilderStore builderStore,
    List<BuilderStoreImage> images,
    ScoreSnapshot latestScore,
    boolean liked,
    boolean bookmarked) {

  public BuilderStoreDetailResult {
    images = images == null ? List.of() : List.copyOf(images);
  }
}
