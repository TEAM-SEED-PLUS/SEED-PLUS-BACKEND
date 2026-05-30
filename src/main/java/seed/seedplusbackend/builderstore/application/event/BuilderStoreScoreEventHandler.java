package seed.seedplusbackend.builderstore.application.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStore;
import seed.seedplusbackend.builderstore.domain.repository.BuilderStoreRepository;
import seed.seedplusbackend.score.domain.entity.ScoreGrade;
import seed.seedplusbackend.score.domain.entity.ScoreSnapshot;
import seed.seedplusbackend.score.domain.entity.ScoreTargetType;
import seed.seedplusbackend.score.domain.entity.ScoreType;
import seed.seedplusbackend.score.domain.repository.ScoreSnapshotRepository;

@Component
@RequiredArgsConstructor
public class BuilderStoreScoreEventHandler {

  private final BuilderStoreRepository builderStoreRepository;
  private final ScoreSnapshotRepository scoreSnapshotRepository;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(BuilderStoreCreatedEvent event) {
    Long builderStoreId = event.builderStoreId();
    LocalDate referenceMonth = LocalDate.now().withDayOfMonth(1);
    if (scoreSnapshotRepository.existsByBuilderStoreIdAndTargetTypeAndScoreTypeAndReferenceMonth(
        builderStoreId, ScoreTargetType.BUILDER_STORE, ScoreType.PROPERTY, referenceMonth)) {
      return;
    }

    BuilderStore builderStore = builderStoreRepository.findById(builderStoreId).orElse(null);
    if (builderStore == null) {
      return;
    }

    scoreSnapshotRepository.save(
        ScoreSnapshot.builder()
            .targetType(ScoreTargetType.BUILDER_STORE)
            .store(null)
            .builderStore(builderStore)
            .commercialArea(null)
            .region(null)
            .scoreType(ScoreType.PROPERTY)
            .referenceMonth(referenceMonth)
            .totalScore(new BigDecimal("0.00"))
            .grade(ScoreGrade.E)
            .pd(null)
            .survivalProbability(null)
            .build());
  }
}
