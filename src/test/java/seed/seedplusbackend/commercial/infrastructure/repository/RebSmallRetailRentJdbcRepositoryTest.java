package seed.seedplusbackend.commercial.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import seed.seedplusbackend.commercial.application.port.RebSmallRetailRentStorePort;
import seed.seedplusbackend.commercial.application.result.RebSmallRetailRentFileResult;
import seed.seedplusbackend.commercial.application.result.RebSmallRetailRentPeriod;
import seed.seedplusbackend.commercial.application.result.RebSmallRetailRentRowResult;
import seed.seedplusbackend.support.AbstractPostgresContainerTest;
import seed.seedplusbackend.support.RepositoryTest;

@RepositoryTest
@Import(RebSmallRetailRentJdbcRepository.class)
@DisplayName("한국부동산원 소규모상가 임대료 저장소")
class RebSmallRetailRentJdbcRepositoryTest extends AbstractPostgresContainerTest {

  @Autowired private RebSmallRetailRentStorePort storePort;
  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  @DisplayName("수정 파일에 포함된 지역만 갱신하고 나머지 지역과 분기는 보존한다")
  void replace_updatesOnlyRowsIncludedInFile() {
    RebSmallRetailRentPeriod period = new RebSmallRetailRentPeriod(2026, 1);
    RebSmallRetailRentPeriod previousPeriod = new RebSmallRetailRentPeriod(2025, 4);
    storePort.replace(
        "first.csv",
        "first-hash",
        new RebSmallRetailRentFileResult(
            List.of(previousPeriod, period),
            List.of(
                row("national", "전국", 2025, 4, "20.5"),
                row("national", "전국", 2026, 1, "20.6"),
                row("seoul", "서울", 2026, 1, "52.2"))));

    storePort.replace(
        "corrected.csv",
        "corrected-hash",
        new RebSmallRetailRentFileResult(List.of(period), List.of(row("national", "전국", "20.7"))));

    assertThat(metricCount()).isEqualTo(3);
    assertThat(rent("national", 2026, 1)).isEqualByComparingTo("20.7");
    assertThat(rent("seoul", 2026, 1)).isEqualByComparingTo("52.2");
    assertThat(rent("national", 2025, 4)).isEqualByComparingTo("20.5");
    assertThat(sourceFileName("national", 2026, 1)).isEqualTo("corrected.csv");
  }

  private RebSmallRetailRentRowResult row(String key, String name, String rent) {
    return row(key, name, 2026, 1, rent);
  }

  private RebSmallRetailRentRowResult row(
      String key, String name, int year, int quarter, String rent) {
    return new RebSmallRetailRentRowResult(
        key, 1, name, name, 1, year, quarter, new BigDecimal(rent));
  }

  private Integer metricCount() {
    return jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM reb_small_retail_rent_metrics", Integer.class);
  }

  private BigDecimal rent(String sourceAreaKey, int year, int quarter) {
    return jdbcTemplate.queryForObject(
        """
        SELECT metric.rent_per_square_meter_thousand_krw
        FROM reb_small_retail_rent_metrics metric
        JOIN reb_small_retail_rent_areas area
          ON area.reb_small_retail_rent_area_id = metric.reb_small_retail_rent_area_id
        WHERE area.source_area_key = ?
          AND metric.reference_year = ?
          AND metric.reference_quarter = ?
        """,
        BigDecimal.class,
        sourceAreaKey,
        year,
        quarter);
  }

  private String sourceFileName(String sourceAreaKey, int year, int quarter) {
    return jdbcTemplate.queryForObject(
        """
        SELECT metric.source_file_name
        FROM reb_small_retail_rent_metrics metric
        JOIN reb_small_retail_rent_areas area
          ON area.reb_small_retail_rent_area_id = metric.reb_small_retail_rent_area_id
        WHERE area.source_area_key = ?
          AND metric.reference_year = ?
          AND metric.reference_quarter = ?
        """,
        String.class,
        sourceAreaKey,
        year,
        quarter);
  }
}
