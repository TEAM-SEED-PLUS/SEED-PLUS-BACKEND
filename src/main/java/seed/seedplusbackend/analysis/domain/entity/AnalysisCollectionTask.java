package seed.seedplusbackend.analysis.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seed.seedplusbackend.global.common.BaseTimeEntity;

@Getter
@Entity
@Table(
    name = "analysis_collection_tasks",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_analysis_collection_task",
          columnNames = {"analysis_collection_run_id", "data_type", "target_key"})
    },
    indexes = {
      @Index(
          name = "idx_analysis_collection_tasks_run_status",
          columnList = "analysis_collection_run_id,status")
    })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnalysisCollectionTask extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "analysis_collection_task_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "analysis_collection_run_id", nullable = false)
  private AnalysisCollectionRun run;

  @Column(name = "data_type", nullable = false, length = 100)
  private String dataType;

  @Column(name = "target_key", nullable = false, length = 100)
  private String targetKey;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private AnalysisCollectionTaskStatus status;

  @Column(name = "attempt_count", nullable = false)
  private int attemptCount;

  @Column(name = "error_message", length = 2000)
  private String errorMessage;

  @Column(name = "started_at")
  private OffsetDateTime startedAt;

  @Column(name = "finished_at")
  private OffsetDateTime finishedAt;

  private AnalysisCollectionTask(AnalysisCollectionRun run, String dataType, String targetKey) {
    this.run = run;
    this.dataType = dataType;
    this.targetKey = targetKey;
    this.status = AnalysisCollectionTaskStatus.PENDING;
  }

  public static AnalysisCollectionTask create(
      AnalysisCollectionRun run, String dataType, String targetKey) {
    return new AnalysisCollectionTask(run, dataType, targetKey);
  }

  public void start() {
    if (status != AnalysisCollectionTaskStatus.PENDING
        && status != AnalysisCollectionTaskStatus.FAILED) {
      throw new IllegalStateException("대기 또는 실패한 수집 작업만 시작할 수 있습니다.");
    }
    status = AnalysisCollectionTaskStatus.RUNNING;
    attemptCount++;
    errorMessage = null;
    startedAt = OffsetDateTime.now();
    finishedAt = null;
  }

  public void complete() {
    requireRunning();
    status = AnalysisCollectionTaskStatus.COMPLETED;
    finishedAt = OffsetDateTime.now();
  }

  public void fail(String errorMessage) {
    requireRunning();
    status = AnalysisCollectionTaskStatus.FAILED;
    this.errorMessage = errorMessage;
    finishedAt = OffsetDateTime.now();
  }

  private void requireRunning() {
    if (status != AnalysisCollectionTaskStatus.RUNNING) {
      throw new IllegalStateException("실행 중인 수집 작업만 완료 또는 실패 처리할 수 있습니다.");
    }
  }
}
