package seed.seedplusbackend.builderstore.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seed.seedplusbackend.global.common.BaseCreatedEntity;
import seed.seedplusbackend.user.domain.entity.User;

@Getter
@Entity
@Table(
    name = "builder_store_bookmarks",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_builder_store_bookmarks",
          columnNames = {"builder_store_id", "user_id"})
    })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BuilderStoreBookmark extends BaseCreatedEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "builder_store_bookmark_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "builder_store_id", nullable = false)
  private BuilderStore builderStore;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "estimated_sales_quarter", length = 5)
  private String estimatedSalesQuarter;

  @Column(name = "estimated_sales_amount")
  private Long estimatedSalesAmount;

  @Column(name = "business_survival_year")
  private Integer businessSurvivalYear;

  @Column(name = "survival_rate", precision = 7, scale = 3)
  private BigDecimal survivalRate;

  @Column(name = "business_count_year")
  private Integer businessCountYear;

  @Column(name = "active_business_count", precision = 20, scale = 3)
  private BigDecimal activeBusinessCount;

  @Column(name = "new_business_count", precision = 20, scale = 3)
  private BigDecimal newBusinessCount;

  @Column(name = "closed_business_count", precision = 20, scale = 3)
  private BigDecimal closedBusinessCount;

  @Column(name = "store_info_collected_at")
  private OffsetDateTime storeInfoCollectedAt;

  @Column(name = "store_count")
  private Integer storeCount;

  @Column(name = "rent_reference_year")
  private Integer rentReferenceYear;

  @Column(name = "rent_reference_quarter")
  private Integer rentReferenceQuarter;

  @Column(name = "rent_per_square_meter_thousand_krw", precision = 12, scale = 3)
  private BigDecimal rentPerSquareMeterThousandKrw;

  @Column(name = "data_refreshed_at")
  private OffsetDateTime dataRefreshedAt;

  @Builder
  private BuilderStoreBookmark(
      BuilderStore builderStore, User user, BuilderStoreBookmarkSnapshot snapshot) {
    this.builderStore = builderStore;
    this.user = user;
    applySnapshot(snapshot);
  }

  public void applySnapshot(BuilderStoreBookmarkSnapshot snapshot) {
    if (snapshot == null) {
      return;
    }
    this.estimatedSalesQuarter = snapshot.estimatedSalesQuarter();
    this.estimatedSalesAmount = snapshot.estimatedSalesAmount();
    this.businessSurvivalYear = snapshot.businessSurvivalYear();
    this.survivalRate = snapshot.survivalRate();
    this.businessCountYear = snapshot.businessCountYear();
    this.activeBusinessCount = snapshot.activeBusinessCount();
    this.newBusinessCount = snapshot.newBusinessCount();
    this.closedBusinessCount = snapshot.closedBusinessCount();
    this.storeInfoCollectedAt = snapshot.storeInfoCollectedAt();
    this.storeCount = snapshot.storeCount();
    this.rentReferenceYear = snapshot.rentReferenceYear();
    this.rentReferenceQuarter = snapshot.rentReferenceQuarter();
    this.rentPerSquareMeterThousandKrw = snapshot.rentPerSquareMeterThousandKrw();
    this.dataRefreshedAt = OffsetDateTime.now();
  }

  public boolean isOwnedBy(Long userId) {
    return user != null && user.getId().equals(userId);
  }
}
