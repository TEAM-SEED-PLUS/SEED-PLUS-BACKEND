package seed.seedplusbackend.store.domain.entity;

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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seed.seedplusbackend.global.common.BaseCreatedEntity;

@Getter
@Entity
@Table(
    name = "store_images",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_store_images_store_url",
          columnNames = {"store_id", "image_url"})
    })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreImage extends BaseCreatedEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "store_image_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "store_id", nullable = false)
  private Store store;

  @Column(name = "image_url", nullable = false, length = 500)
  private String imageUrl;

  @Column(name = "display_order", nullable = false)
  private int displayOrder;

  @Builder
  private StoreImage(Store store, String imageUrl, int displayOrder) {
    this.store = store;
    this.imageUrl = imageUrl;
    this.displayOrder = displayOrder;
  }
}
