package seed.seedplusbackend.analysis.domain.entity;

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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seed.seedplusbackend.global.common.BaseTimeEntity;
import seed.seedplusbackend.user.domain.entity.User;

@Getter
@Entity
@Table(name = "analysis_collection_runs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnalysisCollectionRun extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "analysis_collection_run_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(name = "analysis_type", nullable = false, length = 20)
  private AnalysisCollectionType analysisType;

  @Column(name = "region_code", nullable = false, length = 30)
  private String regionCode;

  @Column(name = "industry_code", nullable = false, length = 50)
  private String industryCode;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private AnalysisCollectionRunStatus status;

  private AnalysisCollectionRun(
      User user, AnalysisCollectionType analysisType, String regionCode, String industryCode) {
    this.user = user;
    this.analysisType = analysisType;
    this.regionCode = regionCode;
    this.industryCode = industryCode;
    this.status = AnalysisCollectionRunStatus.PENDING;
  }

  public static AnalysisCollectionRun create(
      User user, AnalysisCollectionType analysisType, String regionCode, String industryCode) {
    return new AnalysisCollectionRun(user, analysisType, regionCode, industryCode);
  }

  public void start() {
    status = AnalysisCollectionRunStatus.RUNNING;
  }

  public void complete() {
    status = AnalysisCollectionRunStatus.COMPLETED;
  }

  public void fail() {
    status = AnalysisCollectionRunStatus.FAILED;
  }
}
