package seed.seedplusbackend.store.application;

import seed.seedplusbackend.score.domain.entity.ScoreSnapshot;
import seed.seedplusbackend.store.domain.entity.Store;

public record StoreDetailResult(Store store, ScoreSnapshot latestScore) {}
