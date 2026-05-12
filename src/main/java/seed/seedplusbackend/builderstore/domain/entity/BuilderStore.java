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
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seed.seedplusbackend.building.domain.entity.Building;
import seed.seedplusbackend.commercial.domain.entity.CommercialArea;
import seed.seedplusbackend.global.common.BaseTimeEntity;
import seed.seedplusbackend.industry.domain.entity.Industry;
import seed.seedplusbackend.region.domain.entity.Region;
import seed.seedplusbackend.user.domain.entity.User;

@Getter
@Entity
@Table(name = "builder_stores")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BuilderStore extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "builder_store_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "region_id", nullable = false)
  private Region region;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "commercial_area_id", nullable = false)
  private CommercialArea commercialArea;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "industry_id", nullable = false)
  private Industry industry;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "base_building_id")
  private Building baseBuilding;

  @Column(name = "name", nullable = false, length = 150)
  private String name;

  @Column(name = "area", nullable = false)
  private int area;

  @Column(name = "expected_monthly_sales", nullable = false)
  private long expectedMonthlySales;

  @Column(name = "expected_profit_rate", nullable = false, precision = 6, scale = 2)
  private BigDecimal expectedProfitRate;

  @Column(name = "investment_payback_months", nullable = false)
  private int investmentPaybackMonths;

  @Column(name = "property_score", nullable = false)
  private int propertyScore;

  @Column(name = "monthly_rent", nullable = false)
  private long monthlyRent;

  @Column(name = "deposit", nullable = false)
  private long deposit;

  @Column(name = "investment_amount", nullable = false)
  private long investmentAmount;

  @Column(name = "description", length = 1000)
  private String description;

  @Column(name = "like_count", nullable = false)
  private long likeCount;

  @Column(name = "comment_count", nullable = false)
  private long commentCount;

  @Enumerated(EnumType.STRING)
  @Column(name = "visibility_status", nullable = false, length = 20)
  private BuilderStoreVisibilityStatus visibilityStatus;

  @Column(name = "uploaded_at", nullable = false)
  private OffsetDateTime uploadedAt;

  @Builder
  private BuilderStore(
      User user,
      Region region,
      CommercialArea commercialArea,
      Industry industry,
      Building baseBuilding,
      String name,
      int area,
      long expectedMonthlySales,
      BigDecimal expectedProfitRate,
      int investmentPaybackMonths,
      int propertyScore,
      long monthlyRent,
      long deposit,
      long investmentAmount,
      String description,
      long likeCount,
      long commentCount,
      BuilderStoreVisibilityStatus visibilityStatus,
      OffsetDateTime uploadedAt) {
    this.user = user;
    this.region = region;
    this.commercialArea = commercialArea;
    this.industry = industry;
    this.baseBuilding = baseBuilding;
    this.name = name;
    this.area = area;
    this.expectedMonthlySales = expectedMonthlySales;
    this.expectedProfitRate = expectedProfitRate;
    this.investmentPaybackMonths = investmentPaybackMonths;
    this.propertyScore = propertyScore;
    this.monthlyRent = monthlyRent;
    this.deposit = deposit;
    this.investmentAmount = investmentAmount;
    this.description = description;
    this.likeCount = likeCount;
    this.commentCount = commentCount;
    this.visibilityStatus = visibilityStatus;
    this.uploadedAt = uploadedAt;
  }
}
