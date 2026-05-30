package seed.seedplusbackend.store.domain.entity;

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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seed.seedplusbackend.building.domain.entity.Building;
import seed.seedplusbackend.global.common.BaseTimeEntity;
import seed.seedplusbackend.industry.domain.entity.Industry;

@Getter
@Entity
@Table(name = "stores")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Store extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "store_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "building_id", nullable = false)
  private Building building;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "industry_id", nullable = false)
  private Industry industry;

  @Column(name = "floor", nullable = false, length = 30)
  private String floor;

  @Column(name = "name", nullable = false, length = 150)
  private String name;

  @Column(name = "code", length = 80)
  private String code;

  @Column(name = "area", nullable = false)
  private int area;

  @Column(name = "deposit", nullable = false)
  private long deposit;

  @Column(name = "monthly_rent", nullable = false)
  private long monthlyRent;

  @Column(name = "is_vacant", nullable = false)
  private boolean vacant;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private StoreStatus status;

  @Builder
  private Store(
      Building building,
      Industry industry,
      String floor,
      String name,
      String code,
      int area,
      long deposit,
      long monthlyRent,
      boolean vacant,
      StoreStatus status) {
    this.building = building;
    this.industry = industry;
    this.floor = floor;
    this.name = name;
    this.code = code;
    this.area = area;
    this.deposit = deposit;
    this.monthlyRent = monthlyRent;
    this.vacant = vacant;
    this.status = status;
  }
}
