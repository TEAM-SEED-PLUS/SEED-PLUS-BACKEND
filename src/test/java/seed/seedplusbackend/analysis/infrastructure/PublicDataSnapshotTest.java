package seed.seedplusbackend.analysis.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import seed.seedplusbackend.analysis.application.port.PublicDataResolver;
import seed.seedplusbackend.support.AbstractPostgresContainerTest;

@DisplayName("계산용 공공데이터의 일관된 조회 시점")
class PublicDataSnapshotTest extends AbstractPostgresContainerTest {
  @Test
  @DisplayName("조회 도중 다른 연결에서 반영한 데이터는 다음 계산 요청부터 사용한다")
  void readsOneSnapshotAcrossQueries() {
    var ds =
        new DriverManagerDataSource(
            POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    Flyway.configure().dataSource(ds).locations("classpath:db/migration").load().migrate();
    // 별도 DataSource 객체로 작성하여 조회 트랜잭션에 참여하지 않는 독립 연결을 사용한다.
    var writer =
        new JdbcTemplate(
            new DriverManagerDataSource(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword()));
    String storeId = UUID.randomUUID().toString().substring(0, 24);
    String regionCode = "9999999997";
    String industryCode = UUID.randomUUID().toString().substring(0, 6);
    var inserted = new AtomicBoolean();
    var reader =
        new JdbcTemplate(ds) {
          @Override
          public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
            List<T> rows = super.query(sql, rowMapper, args);
            if (inserted.compareAndSet(false, true)) {
              writer.update(
                  "INSERT INTO small_business_stores (store_id, commercial_area_code, legal_dong_code, small_industry_code) VALUES (?, 'snapshot-test', ?, ?)",
                  storeId,
                  regionCode,
                  industryCode);
            }
            return rows;
          }
        };
    var interceptor = new TransactionInterceptor();
    interceptor.setTransactionManager(new DataSourceTransactionManager(ds));
    interceptor.setTransactionAttributeSource(new AnnotationTransactionAttributeSource());
    var factory = new ProxyFactory(new JdbcPublicDataResolver(reader));
    factory.addAdvice(interceptor);
    var resolver = (PublicDataResolver) factory.getProxy();
    try {
      assertThat(resolver.resolve(regionCode, industryCode).storeCountInCommercialArea()).isZero();
      assertThat(resolver.resolve(regionCode, industryCode).storeCountInCommercialArea())
          .isEqualTo(1);
    } finally {
      writer.update("DELETE FROM small_business_stores WHERE store_id = ?", storeId);
    }
  }
}
