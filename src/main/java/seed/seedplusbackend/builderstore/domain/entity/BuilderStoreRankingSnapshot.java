package seed.seedplusbackend.builderstore.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seed.seedplusbackend.global.common.BaseTimeEntity;

@Getter
@Entity
@Table(
    name = "builder_store_ranking_snapshots",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_builder_store_ranking",
          columnNames = {"builder_store_id", "ranking_type", "reference_month"})
    })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BuilderStoreRankingSnapshot extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "builder_store_ranking_snapshot_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "builder_store_id", nullable = false)
  private BuilderStore builderStore;

  @Enumerated(EnumType.STRING)
  @Column(name = "ranking_type", nullable = false, length = 30)
  private BuilderStoreRankingType rankingType;

  @Column(name = "ranking", nullable = false)
  private int ranking;

  @Column(name = "reference_month", nullable = false)
  private LocalDate referenceMonth;

  @Builder
  private BuilderStoreRankingSnapshot(
      BuilderStore builderStore,
      BuilderStoreRankingType rankingType,
      int ranking,
      LocalDate referenceMonth) {
    this.builderStore = builderStore;
    this.rankingType = rankingType;
    this.ranking = ranking;
    this.referenceMonth = referenceMonth;
  }
}
