package seed.seedplusbackend.commercial.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "commercial_data_collect_histories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommercialDataCollectHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "commercial_data_collect_history_id")
  private Long id;

  @Column(name = "data_type", nullable = false, length = 100)
  private String dataType;

  @Column(name = "target_key", nullable = false, length = 50)
  private String targetKey;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 30)
  private CommercialDataCollectStatus status;

  @Column(name = "total_count", nullable = false)
  private Long totalCount = 0L;

  @Column(name = "fetched_count", nullable = false)
  private Long fetchedCount = 0L;

  @Column(name = "last_start_index", nullable = false)
  private Long lastStartIndex = 0L;

  @Column(name = "error_message", length = 2000)
  private String errorMessage;

  @Column(name = "started_at", nullable = false)
  private OffsetDateTime startedAt;

  @Column(name = "finished_at")
  private OffsetDateTime finishedAt;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at")
  private OffsetDateTime updatedAt;

  private CommercialDataCollectHistory(String dataType, String targetKey) {
    this.dataType = dataType;
    this.targetKey = targetKey;
    this.status = CommercialDataCollectStatus.RUNNING;
    this.startedAt = OffsetDateTime.now();
    this.createdAt = OffsetDateTime.now();
  }

  public static CommercialDataCollectHistory start(String dataType, String targetKey) {
    return new CommercialDataCollectHistory(dataType, targetKey);
  }

  public void updateProgress(long totalCount, long fetchedCount, long lastStartIndex) {
    this.totalCount = totalCount;
    this.fetchedCount = fetchedCount;
    this.lastStartIndex = lastStartIndex;
    this.updatedAt = OffsetDateTime.now();
  }

  public void complete(long totalCount, long fetchedCount) {
    this.status = CommercialDataCollectStatus.COMPLETED;
    this.totalCount = totalCount;
    this.fetchedCount = fetchedCount;
    this.finishedAt = OffsetDateTime.now();
    this.updatedAt = OffsetDateTime.now();
  }

  public void fail(long totalCount, long fetchedCount, long lastStartIndex, String errorMessage) {
    this.status = CommercialDataCollectStatus.FAILED;
    this.totalCount = totalCount;
    this.fetchedCount = fetchedCount;
    this.lastStartIndex = lastStartIndex;
    this.errorMessage = errorMessage;
    this.finishedAt = OffsetDateTime.now();
    this.updatedAt = OffsetDateTime.now();
  }
}
