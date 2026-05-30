package seed.seedplusbackend.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import seed.seedplusbackend.global.common.JpaAuditingConfig;

/**
 * Repository 통합 테스트용 메타 어노테이션.
 *
 * <p>실제 Postgres/PostGIS 컨테이너에 접속해 Flyway 마이그레이션을 그대로 적용한 뒤 검증한다.
 *
 * <ul>
 *   <li>{@link DataJpaTest}: JPA 슬라이스 테스트
 *   <li>{@code Replace.NONE}: Testcontainers 기반 실제 DataSource 사용
 *   <li>{@link FlywayAutoConfiguration}: Flyway 마이그레이션 적용 보장
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@Import(JpaAuditingConfig.class)
public @interface RepositoryTest {}
