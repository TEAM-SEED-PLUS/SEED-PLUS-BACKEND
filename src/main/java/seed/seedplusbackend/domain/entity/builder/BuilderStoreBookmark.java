package seed.seedplusbackend.domain.entity.builder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seed.seedplusbackend.domain.entity.BaseEntity;
import seed.seedplusbackend.domain.entity.user.StoreOwner;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(
    name = "builder_store_bookmark",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_builder_store_bookmark_user_store",
            columnNames = {"bookmarker_id", "builder_store_id"}
        )
    }
)
public class BuilderStoreBookmark extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "bookmarker_id", nullable = false)
  private StoreOwner bookmarker;

  @Column(name = "builder_store_id", nullable = false)
  private Long builderStoreId;
}
