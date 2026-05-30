package seed.seedplusbackend.industry.domain.entity;

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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
    name = "industries",
    uniqueConstraints = {
      @UniqueConstraint(name = "industries_industry_code_key", columnNames = "industry_code")
    })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Industry {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "industry_id")
  private Long id;

  @Column(name = "industry_code", nullable = false, length = 50)
  private String industryCode;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_industry_id")
  private Industry parentIndustry;

  @Enumerated(EnumType.STRING)
  @Column(name = "level", nullable = false, length = 20)
  private IndustryLevel level;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private IndustryStatus status;

  @Builder
  private Industry(
      String industryCode,
      String name,
      Industry parentIndustry,
      IndustryLevel level,
      IndustryStatus status) {
    this.industryCode = industryCode;
    this.name = name;
    this.parentIndustry = parentIndustry;
    this.level = level;
    this.status = status;
  }
}
