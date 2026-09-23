package seed.seedplusbackend.commercial.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.support.TransactionTemplate;
import seed.seedplusbackend.commercial.application.result.*;
import seed.seedplusbackend.support.AbstractPostgresContainerTest;

@DisplayName("공공데이터 변경 시에만 갱신하는 upsert")
class PublicDataUpsertTest extends AbstractPostgresContainerTest {
  private static JdbcTemplate jdbc;
  private static TransactionTemplate transaction;

  enum Source {
    SDOT(
        "seoul_sdot_foot_traffic",
        "serial_number",
        "serialNumber",
        "visitor_count",
        "visitorCount",
        "model_name",
        "modelName"),
    SALES(
        "commercial_estimated_sales",
        "trdar_cd",
        "trdarCd",
        "agrde_60_above_selng_co",
        "agrde60AboveSelngCo",
        "trdar_cd_nm",
        "trdarCdNm"),
    STORES(
        "small_business_stores",
        "store_id",
        "storeId",
        "longitude",
        "longitude",
        "branch_name",
        "branchName"),
    COUNT(
        "kosis_business_counts",
        "table_id",
        "tableId",
        "business_count",
        "businessCount",
        "source_updated_at",
        "sourceUpdatedAt"),
    SURVIVAL(
        "kosis_business_survival_rates",
        "table_id",
        "tableId",
        "survival_rate",
        "survivalRate",
        "source_updated_at",
        "sourceUpdatedAt"),
    CITY(
        "seoul_realtime_city_populations",
        "area_code",
        "areaCode",
        "population_max",
        "populationMax",
        "congestion_message",
        "congestionMessage");
    final String table,
        keyColumn,
        keyField,
        metricColumn,
        metricField,
        nullableColumn,
        nullableField;

    Source(
        String table,
        String keyColumn,
        String keyField,
        String metricColumn,
        String metricField,
        String nullableColumn,
        String nullableField) {
      this.table = table;
      this.keyColumn = keyColumn;
      this.keyField = keyField;
      this.metricColumn = metricColumn;
      this.metricField = metricField;
      this.nullableColumn = nullableColumn;
      this.nullableField = nullableField;
    }
  }

  @BeforeAll
  static void setupDatabase() {
    var ds =
        new DriverManagerDataSource(
            POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    Flyway.configure().dataSource(ds).locations("classpath:db/migration").load().migrate();
    jdbc = new JdbcTemplate(ds);
    transaction = new TransactionTemplate(new DataSourceTransactionManager(ds));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(Source.class)
  @DisplayName("같은 값을 재수집하면 행 버전과 수집 시각을 변경하지 않는다")
  void identicalDataDoesNotUpdate(Source source) {
    transaction.executeWithoutResult(
        status -> {
          status.setRollbackOnly();
          var values = values(source);
          values.put(source.nullableField, null);
          save(source, values);
          jdbc.update(
              "UPDATE "
                  + source.table
                  + " SET collected_at = '2000-01-01T00:00:00Z', updated_at = '2000-01-01T00:00:00Z' WHERE "
                  + source.keyColumn
                  + " = ?",
              values.get(source.keyField));
          var before = snapshot(source, values);
          save(source, values);
          assertThat(snapshot(source, values)).isEqualTo(before);
        });
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(Source.class)
  @DisplayName("같은 키의 실제 수치가 바뀌면 새 행을 만들지 않고 갱신한다")
  void changedMetricUpdatesExistingRow(Source source) {
    transaction.executeWithoutResult(
        status -> {
          status.setRollbackOnly();
          var values = values(source);
          save(source, values);
          var before = snapshot(source, values);
          values.put(
              source.metricField,
              source == Source.SDOT || source == Source.CITY ? 20L : new BigDecimal("20"));
          save(source, values);
          assertThat(snapshot(source, values).get("row_version"))
              .isNotEqualTo(before.get("row_version"));
          assertThat(
                  jdbc.queryForObject(
                      "SELECT "
                          + source.metricColumn
                          + " FROM "
                          + source.table
                          + " WHERE "
                          + source.keyColumn
                          + " = ?",
                      BigDecimal.class,
                      values.get(source.keyField)))
              .isEqualByComparingTo("20");
        });
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(Source.class)
  @DisplayName("NULL에서 값으로 또는 값에서 NULL로 바뀌는 변경도 반영한다")
  void nullableChangesArePersisted(Source source) {
    transaction.executeWithoutResult(
        status -> {
          status.setRollbackOnly();
          var values = values(source);
          values.put(source.nullableField, null);
          save(source, values);
          values.put(source.nullableField, "changed");
          save(source, values);
          String sql =
              "SELECT "
                  + source.nullableColumn
                  + " FROM "
                  + source.table
                  + " WHERE "
                  + source.keyColumn
                  + " = ?";
          assertThat(jdbc.queryForObject(sql, String.class, values.get(source.keyField)))
              .isEqualTo("changed");
          values.put(source.nullableField, null);
          save(source, values);
          assertThat(jdbc.queryForObject(sql, String.class, values.get(source.keyField))).isNull();
        });
  }

  private Map<String, Object> values(Source source) {
    var values = new HashMap<String, Object>();
    values.put(source.keyField, UUID.randomUUID().toString().substring(0, 8));
    return values;
  }

  private Map<String, Object> snapshot(Source source, Map<String, Object> values) {
    return jdbc.queryForMap(
        "SELECT ctid::text AS row_version, collected_at, updated_at FROM "
            + source.table
            + " WHERE "
            + source.keyColumn
            + " = ?",
        values.get(source.keyField));
  }

  private void save(Source source, Map<String, Object> values) {
    switch (source) {
      case SDOT ->
          new SeoulSdotFootTrafficJdbcRepository(jdbc)
              .upsertAll(List.of(row(SeoulSdotFootTrafficRowResult.class, values)));
      case SALES ->
          new CommercialEstimatedSalesJdbcRepository(jdbc)
              .upsertAll(List.of(row(CommercialEstimatedSalesRowResult.class, values)));
      case STORES ->
          new SmallBusinessStoreJdbcRepository(jdbc)
              .upsertAll("test", List.of(row(SmallBusinessStoreRowResult.class, values)));
      case COUNT ->
          new KosisBusinessCountJdbcRepository(jdbc)
              .upsertAll(List.of(row(KosisBusinessCountRowResult.class, values)));
      case SURVIVAL ->
          new KosisBusinessSurvivalJdbcRepository(jdbc)
              .upsertAll(List.of(row(KosisBusinessSurvivalRowResult.class, values)));
      case CITY ->
          new SeoulRealtimeCityPopulationJdbcRepository(jdbc)
              .upsert(row(SeoulRealtimeCityPopulationResult.class, values));
    }
  }

  // 필드가 많은 매출 레코드도 유효한 초기값으로 구성하고 검증할 변경만 덮어쓴다.
  private <T> T row(Class<T> type, Map<String, Object> overrides) {
    var fields = type.getRecordComponents();
    Object[] values =
        Arrays.stream(fields)
            .map(
                field -> {
                  if (overrides.containsKey(field.getName())) return overrides.get(field.getName());
                  if (field.getName().equals("stdrYyquCd")) return "20261";
                  if (field.getType() == String.class) return "T";
                  if (field.getType() == int.class) return 2026;
                  if (field.getType() == long.class) return 10L;
                  if (field.getType() == boolean.class) return false;
                  if (field.getType() == BigDecimal.class) return BigDecimal.TEN;
                  if (field.getType() == LocalDateTime.class)
                    return LocalDateTime.of(2026, 9, 16, 0, 0);
                  throw new IllegalArgumentException("지원하지 않는 테스트 필드: " + field.getName());
                })
            .toArray();
    try {
      return type.getDeclaredConstructor(
              Arrays.stream(fields)
                  .map(java.lang.reflect.RecordComponent::getType)
                  .toArray(Class<?>[]::new))
          .newInstance(values);
    } catch (ReflectiveOperationException exception) {
      throw new IllegalStateException(exception);
    }
  }
}
