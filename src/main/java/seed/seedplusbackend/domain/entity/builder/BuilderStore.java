package seed.seedplusbackend.domain.entity.builder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seed.seedplusbackend.domain.entity.BaseEntity;
import seed.seedplusbackend.domain.entity.base.Industry;
import seed.seedplusbackend.domain.entity.base.Region;
import seed.seedplusbackend.domain.entity.user.StoreOwner;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "builder_store")
public class BuilderStore extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "store_owner_id", nullable = false)
  private StoreOwner storeOwner;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(nullable = false)
  private Industry industry;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(nullable = false)
  private Region region;

  @Column(nullable = false, length = 150)
  private String storeName;

  @Column(nullable = false)
  private Integer area; // ㎡

  @Column(nullable = false)
  private Long expectedMonthlySales; // 원 or 만원 기준은 프로젝트 전역에서 통일

  @Column(nullable = false)
  private Double expectedProfitRate; // %

  @Column(nullable = false)
  private Integer investmentPaybackMonths;

  @Column(nullable = false)
  private Integer propertyScore;

  private Long monthlyRent; // 월세

  @Column(length = 1000)
  private String description;

  @Column(name = "like_count", nullable = false)
  private Long likeCount;

  @Column(name = "comment_count", nullable = false)
  private Long commentCount;

  @Column(nullable = false)
  private Boolean active;

  @Column(name = "uploaded_at", nullable = false)
  private LocalDateTime uploadedAt;
}