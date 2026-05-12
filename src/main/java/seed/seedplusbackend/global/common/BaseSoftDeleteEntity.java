package seed.seedplusbackend.global.common;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.OffsetDateTime;
import lombok.Getter;

/**
 * Soft Delete가 필요한 테이블에 적용하기 위한 베이스 엔티티이다.
 *
 * <p>현재 V1 DDL에는 {@code deleted_at} 컬럼이 존재하지 않으므로, 이 클래스는 향후 DDL에 {@code deleted_at}이 추가되는 시점에
 * 활용한다.
 *
 * <p>실제 사용 시점에는 {@code @SQLRestriction("deleted_at IS NULL")} 및 {@code @SQLDelete}를 함께 적용한다.
 */
@Getter
@MappedSuperclass
public abstract class BaseSoftDeleteEntity extends BaseTimeEntity {

  @Column(name = "deleted_at")
  private OffsetDateTime deletedAt;

  protected void markDeleted(OffsetDateTime deletedAt) {
    this.deletedAt = deletedAt;
  }

  public boolean isDeleted() {
    return deletedAt != null;
  }
}
