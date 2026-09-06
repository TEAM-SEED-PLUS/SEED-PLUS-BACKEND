package seed.seedplusbackend.auth.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("TemporaryPasswordGenerator")
class TemporaryPasswordGeneratorTest {

  @Test
  @DisplayName("임시 비밀번호는 로그인 요청의 길이 제약을 만족하는 12자로 생성된다")
  void generate_returnsPasswordWithFixedLength() {
    String temporaryPassword = TemporaryPasswordGenerator.generate();

    assertThat(temporaryPassword).hasSize(12);
  }

  @Test
  @DisplayName("임시 비밀번호는 영대문자, 영소문자, 숫자, 특수문자를 각각 하나 이상 포함한다")
  void generate_returnsPasswordContainingEveryCharacterType() {
    String temporaryPassword = TemporaryPasswordGenerator.generate();

    assertThat(temporaryPassword.chars().anyMatch(Character::isUpperCase)).isTrue();
    assertThat(temporaryPassword.chars().anyMatch(Character::isLowerCase)).isTrue();
    assertThat(temporaryPassword.chars().anyMatch(Character::isDigit)).isTrue();
    assertThat(temporaryPassword.chars().anyMatch(character -> "!@#$%^&*".indexOf(character) >= 0))
        .isTrue();
  }

  @Test
  @DisplayName("임시 비밀번호는 호출할 때마다 다른 값으로 생성된다")
  void generate_returnsDifferentPasswordOnEachCall() {
    long distinctCount =
        Stream.generate(TemporaryPasswordGenerator::generate).limit(50).distinct().count();

    assertThat(distinctCount).isEqualTo(50);
  }
}
