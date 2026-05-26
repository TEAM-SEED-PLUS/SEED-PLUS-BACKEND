package seed.seedplusbackend.builderstore.application.result;

import java.util.List;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreComment;

public record BuilderStoreCommentResult(
    BuilderStoreComment comment, List<BuilderStoreComment> replies) {

  public BuilderStoreCommentResult {
    replies = replies == null ? List.of() : List.copyOf(replies);
  }
}
