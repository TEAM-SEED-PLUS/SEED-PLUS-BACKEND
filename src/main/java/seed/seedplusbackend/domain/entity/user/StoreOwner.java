package seed.seedplusbackend.domain.entity.user;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.UUID;
import seed.seedplusbackend.domain.entity.BaseEntity;


// 필드 미정 엔티티
@Entity
public class StoreOwner extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;
}
