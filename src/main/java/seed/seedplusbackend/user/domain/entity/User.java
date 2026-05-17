package seed.seedplusbackend.user.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seed.seedplusbackend.global.common.BaseTimeEntity;

@Getter
@Entity
@Table(
    name = "users",
    uniqueConstraints = {
      @UniqueConstraint(name = "users_phone_number_key", columnNames = "phone_number")
    })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_id")
  private Long id;

  @Column(name = "phone_number", nullable = false, length = 20)
  private String phoneNumber;

  @Column(name = "birth_date", nullable = false)
  private LocalDate birthDate;

  @Column(name = "password", nullable = false, length = 255)
  private String password;

  @Column(name = "name", length = 100)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false, length = 20)
  private UserRole role;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private UserStatus status;

  @Builder
  private User(
      String phoneNumber,
      LocalDate birthDate,
      String password,
      String name,
      UserRole role,
      UserStatus status) {
    this.phoneNumber = phoneNumber;
    this.birthDate = birthDate;
    this.password = password;
    this.name = name;
    this.role = role;
    this.status = status;
  }

  public void updateProfile(String name, String password) {
    if (name != null) {
      this.name = name;
    }
    if (password != null) {
      this.password = password;
    }
  }
}
