package seed.seedplusbackend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import seed.seedplusbackend.support.AbstractPostgresContainerTest;

@SpringBootTest(properties = "spring.profiles.active=test")
class SeedPlusBackendApplicationTests extends AbstractPostgresContainerTest {

  @Test
  void contextLoads() {}
}
