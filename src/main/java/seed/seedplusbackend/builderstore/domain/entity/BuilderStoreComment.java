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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seed.seedplusbackend.global.common.BaseTimeEntity;
import seed.seedplusbackend.user.domain.entity.User;

@Getter
@Entity
@Table(name = "builder_store_comments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BuilderStoreComment extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "builder_store_comment_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "builder_store_id", nullable = false)
  private BuilderStore builderStore;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id")
  private BuilderStoreComment parent;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "content", nullable = false, length = 2000)
  private String content;

  @Builder
  private BuilderStoreComment(
      BuilderStore builderStore, BuilderStoreComment parent, User user, String content) {
    this.builderStore = builderStore;
    this.parent = parent;
    this.user = user;
    this.content = content;
  }
}
