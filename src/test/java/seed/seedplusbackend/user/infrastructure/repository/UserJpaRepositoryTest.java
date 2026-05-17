package seed.seedplusbackend.user.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import seed.seedplusbackend.support.AbstractPostgresContainerTest;
import seed.seedplusbackend.support.RepositoryTest;
import seed.seedplusbackend.support.fixture.UserFixture;
import seed.seedplusbackend.user.domain.entity.User;
import seed.seedplusbackend.user.domain.entity.UserRole;
import seed.seedplusbackend.user.domain.entity.UserStatus;

@RepositoryTest
@DisplayName("UserJpaRepository")
class UserJpaRepositoryTest extends AbstractPostgresContainerTest {

  @Autowired private UserJpaRepository userJpaRepository;
  @Autowired private EntityManager entityManager;

  @Test
  @DisplayName("사용자를 저장하면 ID와 createdAt이 자동 부여된다")
  void save_persistsUserAndAssignsIdAndCreatedAt() {
    User user = UserFixture.generalActiveUser("save");

    User saved = userJpaRepository.save(user);

    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getCreatedAt()).isNotNull();
    assertThat(saved.getPhoneNumber()).isNotBlank();
    assertThat(saved.getBirthDate()).isEqualTo(LocalDate.of(1990, 1, 1));
  }

  @Test
  @DisplayName("ID로 사용자를 조회하면 저장된 데이터가 반환된다")
  void findById_returnsSavedUser_whenExists() {
    User saved = userJpaRepository.save(UserFixture.generalActiveUser("find"));

    User found = userJpaRepository.findById(saved.getId()).orElseThrow();

    assertThat(found.getBirthDate()).isEqualTo(LocalDate.of(1990, 1, 1));
  }

  @Test
  @DisplayName("휴대폰 번호로 사용자를 조회하면 저장된 사용자가 반환된다")
  void findByPhoneNumber_returnsSavedUser_whenExists() {
    User saved = userJpaRepository.save(UserFixture.generalActiveUser("01011112222"));

    User found = userJpaRepository.findByPhoneNumber(saved.getPhoneNumber()).orElseThrow();

    assertThat(found.getId()).isEqualTo(saved.getId());
  }

  @Test
  @DisplayName("findAll은 저장된 모든 사용자를 반환한다")
  void findAll_returnsAllUsers() {
    userJpaRepository.save(UserFixture.generalActiveUser("a"));
    userJpaRepository.save(UserFixture.generalActiveUser("b"));

    long count = userJpaRepository.count();

    assertThat(count).isGreaterThanOrEqualTo(2);
  }

  @Test
  @DisplayName("휴대폰 번호가 중복되면 저장 무결성 예외가 발생한다")
  void save_throwsDataIntegrityViolation_whenPhoneNumberDuplicates() {
    String duplicatedPhoneNumber = "01022223333";
    userJpaRepository.save(UserFixture.generalActiveUser(duplicatedPhoneNumber));
    entityManager.flush();

    assertThatThrownBy(
            () -> {
              userJpaRepository.save(
                  UserFixture.generalActiveUser(duplicatedPhoneNumber, LocalDate.of(1991, 1, 1)));
              entityManager.flush();
            })
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  @DisplayName("저장된 사용자는 새 인스턴스 빌더로 구성하면 다른 식별자를 가진다")
  void save_createsDistinctIdentities_whenSavedSeparately() {
    User userA = userJpaRepository.save(UserFixture.generalActiveUser("ia"));
    User userB = userJpaRepository.save(UserFixture.generalActiveUser("ib"));

    assertThat(userA.getId()).isNotEqualTo(userB.getId());
  }

  @Test
  @DisplayName("사용자를 삭제하면 더 이상 조회되지 않는다")
  void deleteById_removesUser() {
    User saved = userJpaRepository.save(UserFixture.generalActiveUser("del"));
    Long id = saved.getId();

    userJpaRepository.deleteById(id);
    entityManager.flush();

    assertThat(userJpaRepository.findById(id)).isEmpty();
  }

  @Test
  @DisplayName("DELETED 상태 사용자도 명시적 조회 시 정상 반환된다")
  void findById_returnsDeletedStatusUser_whenStatusIsDeleted() {
    User saved =
        userJpaRepository.save(
            User.builder()
                .phoneNumber("01099990000")
                .birthDate(LocalDate.of(1990, 1, 1))
                .password("password")
                .name("탈퇴 사용자")
                .role(UserRole.GENERAL)
                .status(UserStatus.DELETED)
                .build());

    User found = userJpaRepository.findById(saved.getId()).orElseThrow();

    assertThat(found.getStatus()).isEqualTo(UserStatus.DELETED);
  }
}
