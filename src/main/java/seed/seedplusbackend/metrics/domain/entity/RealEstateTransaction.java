package seed.seedplusbackend.metrics.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seed.seedplusbackend.building.domain.entity.Building;
import seed.seedplusbackend.global.common.BaseTimeEntity;
import seed.seedplusbackend.region.domain.entity.Region;

@Getter
@Entity
@Table(name = "real_estate_transactions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RealEstateTransaction extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "real_estate_transaction_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "region_id", nullable = false)
  private Region region;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "building_id")
  private Building building;

  @Column(name = "transaction_date", nullable = false)
  private LocalDate transactionDate;

  @Column(name = "transaction_price", nullable = false)
  private long transactionPrice;

  @Column(name = "address", nullable = false, length = 255)
  private String address;

  @Column(name = "area", precision = 12, scale = 2)
  private BigDecimal area;

  @Column(name = "price_per_area")
  private Long pricePerArea;

  @Column(name = "cap_rate", precision = 6, scale = 2)
  private BigDecimal capRate;

  @Column(name = "source_name", length = 100)
  private String sourceName;

  @Column(name = "collected_at")
  private OffsetDateTime collectedAt;

  @Builder
  private RealEstateTransaction(
      Region region,
      Building building,
      LocalDate transactionDate,
      long transactionPrice,
      String address,
      BigDecimal area,
      Long pricePerArea,
      BigDecimal capRate,
      String sourceName,
      OffsetDateTime collectedAt) {
    this.region = region;
    this.building = building;
    this.transactionDate = transactionDate;
    this.transactionPrice = transactionPrice;
    this.address = address;
    this.area = area;
    this.pricePerArea = pricePerArea;
    this.capRate = capRate;
    this.sourceName = sourceName;
    this.collectedAt = collectedAt;
  }
}
