package seed.seedplusbackend.community;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import seed.seedplusbackend.support.AbstractPostgresContainerTest;

@DisplayName("커뮤니티 Flyway 마이그레이션")
class CommunityMigrationTest extends AbstractPostgresContainerTest {
  @Test
  @DisplayName("V1 데이터와 체크섬을 유지하며 V31로 업그레이드한다")
  void upgradesV1WithoutChangingExistingData() throws Exception {
    verifyUpgrade("1", 5);
  }

  @Test
  @DisplayName("현재 V30 DB에서 커뮤니티 V31만 추가한다")
  void upgradesV30WithoutChangingExistingData() throws Exception {
    verifyUpgrade("30", 1);
  }

  private void verifyUpgrade(String baseline, int expectedMigrations) throws Exception {
    String database = "community_migration_" + UUID.randomUUID().toString().replace("-", "");
    try (Connection admin =
            DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
        Statement adminSql = admin.createStatement()) {
      adminSql.execute("create database " + database);
      try {
        String url =
            "jdbc:postgresql://"
                + POSTGRES.getHost()
                + ":"
                + POSTGRES.getMappedPort(5432)
                + "/"
                + database;
        Flyway initial =
            Flyway.configure()
                .dataSource(url, POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration")
                .target(baseline)
                .load();
        initial.migrate();
        Integer checksum = initial.info().applied()[0].getChecksum();
        try (Connection connection =
                DriverManager.getConnection(url, POSTGRES.getUsername(), POSTGRES.getPassword());
            Statement sql = connection.createStatement()) {
          sql.executeUpdate(
              "insert into users(login_id,email,phone_number,password,birth_date,name) values ('migration','migration@test.example','01012345678','test','1990-01-01','보존회원')");
        }
        Flyway latest =
            Flyway.configure()
                .dataSource(url, POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration")
                .target("31")
                .load();
        assertThat(latest.migrate().migrationsExecuted).isEqualTo(expectedMigrations);
        assertThat(latest.info().current().getVersion().toString()).isEqualTo("31");
        assertThat(latest.info().applied()[0].getChecksum()).isEqualTo(checksum);
        latest.validate();
        assertThat(latest.migrate().migrationsExecuted).isZero();
        try (Connection connection =
                DriverManager.getConnection(url, POSTGRES.getUsername(), POSTGRES.getPassword());
            Statement sql = connection.createStatement()) {
          try (var result = sql.executeQuery("select name from users where login_id='migration'")) {
            assertThat(result.next()).isTrue();
            assertThat(result.getString(1)).isEqualTo("보존회원");
          }
          try (var result =
              sql.executeQuery(
                  "select count(*) from information_schema.tables where table_schema='public' and table_name like 'community_%'")) {
            assertThat(result.next()).isTrue();
            assertThat(result.getInt(1)).isEqualTo(5);
          }
        }
      } finally {
        // This database is created only inside the disposable Testcontainers PostgreSQL instance.
        adminSql.execute("drop database " + database + " with (force)");
      }
    }
  }
}
