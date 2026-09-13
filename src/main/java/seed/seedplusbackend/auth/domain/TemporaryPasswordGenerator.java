package seed.seedplusbackend.auth.domain;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TemporaryPasswordGenerator {

  public static final int LENGTH = 12;

  // 사용자가 메일을 보고 직접 입력하므로 혼동하기 쉬운 I, O, l, 0, 1은 후보에서 제외한다.
  private static final String UPPERCASE = "ABCDEFGHJKLMNPQRSTUVWXYZ";
  private static final String LOWERCASE = "abcdefghijkmnopqrstuvwxyz";
  private static final String DIGITS = "23456789";
  private static final String SPECIALS = "!@#$%^&*";
  private static final String ALL = UPPERCASE + LOWERCASE + DIGITS + SPECIALS;
  private static final SecureRandom RANDOM = new SecureRandom();

  private TemporaryPasswordGenerator() {}

  public static String generate() {
    List<Character> characters = new ArrayList<>(LENGTH);
    characters.add(randomCharacter(UPPERCASE));
    characters.add(randomCharacter(LOWERCASE));
    characters.add(randomCharacter(DIGITS));
    characters.add(randomCharacter(SPECIALS));
    while (characters.size() < LENGTH) {
      characters.add(randomCharacter(ALL));
    }
    Collections.shuffle(characters, RANDOM);

    StringBuilder builder = new StringBuilder(LENGTH);
    characters.forEach(builder::append);
    return builder.toString();
  }

  private static char randomCharacter(String candidates) {
    return candidates.charAt(RANDOM.nextInt(candidates.length()));
  }
}
