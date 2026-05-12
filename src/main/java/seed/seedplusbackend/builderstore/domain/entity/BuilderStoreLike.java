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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seed.seedplusbackend.global.common.BaseCreatedEntity;
import seed.seedplusbackend.user.domain.entity.User;

@Getter
@Entity
@Table(
    name = "builder_store_likes",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_builder_store_likes",
          columnNames = {"builder_store_id", "user_id"})
    })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BuilderStoreLike extends BaseCreatedEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "builder_store_like_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "builder_store_id", nullable = false)
  private BuilderStore builderStore;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Builder
  private BuilderStoreLike(BuilderStore builderStore, User user) {
    this.builderStore = builderStore;
    this.user = user;
  }
}
