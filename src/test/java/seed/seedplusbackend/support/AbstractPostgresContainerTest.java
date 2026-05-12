package seed.seedplusbackend.support;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Testcontainers 기반 PostgreSQL/PostGIS 환경에서 Repository 통합 테스트를 실행한다.
 *
 * <p>Docker가 가용하지 않은 환경에서는 {@link Assumptions#assumeTrue}로 명시적으로 스킵하여 실패 원인이 분명히 드러나도록 한다.
 *
 * <p>전체 테스트에서 단일 컨테이너를 재사용하여 부팅 비용을 최소화한다(static + reuse).
 */
@ActiveProfiles("test")
public abstract class AbstractPostgresContainerTest {

  private static final DockerImageName POSTGIS_IMAGE =
      DockerImageName.parse("postgis/postgis:16-3.4").asCompatibleSubstituteFor("postgres");

  @SuppressWarnings("resource")
  protected static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>(POSTGIS_IMAGE)
          .withDatabaseName("seed_test")
          .withUsername("seed")
          .withPassword("seed")
          .withReuse(true);

  static {
    if (DockerClientFactory.instance().isDockerAvailable()) {
      POSTGRES.start();
    }
  }

  @BeforeAll
  static void verifyDockerAvailable() {
    assumeTrue(
        DockerClientFactory.instance().isDockerAvailable(),
        "Docker 환경이 감지되지 않아 Testcontainers 기반 PostgreSQL 테스트를 건너뜁니다.");
  }

  @DynamicPropertySource
  static void registerDataSourceProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
    registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    registry.add("spring.flyway.url", POSTGRES::getJdbcUrl);
    registry.add("spring.flyway.user", POSTGRES::getUsername);
    registry.add("spring.flyway.password", POSTGRES::getPassword);
  }
}
