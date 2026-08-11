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
  @DisplayName("수정 파일을 적재하면 포함된 분기를 통째로 교체한다")
  void replace_replacesRowsInImportedPeriod() {
    RebSmallRetailRentPeriod period = new RebSmallRetailRentPeriod(2026, 1);
    storePort.replace(
        "first.csv",
        "first-hash",
        new RebSmallRetailRentFileResult(
            List.of(period), List.of(row("national", "전국", "20.6"), row("seoul", "서울", "52.2"))));

    storePort.replace(
        "corrected.csv",
        "corrected-hash",
        new RebSmallRetailRentFileResult(List.of(period), List.of(row("national", "전국", "20.7"))));

    assertThat(metricCount()).isEqualTo(1);
    assertThat(rent("national")).isEqualByComparingTo("20.7");
    assertThat(sourceFileName()).isEqualTo("corrected.csv");
  }

  private RebSmallRetailRentRowResult row(String key, String name, String rent) {
    return new RebSmallRetailRentRowResult(key, 1, name, name, 1, 2026, 1, new BigDecimal(rent));
  }

  private Integer metricCount() {
    return jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM reb_small_retail_rent_metrics", Integer.class);
  }

  private BigDecimal rent(String sourceAreaKey) {
    return jdbcTemplate.queryForObject(
        """
        SELECT metric.rent_per_square_meter_thousand_krw
        FROM reb_small_retail_rent_metrics metric
        JOIN reb_small_retail_rent_areas area
          ON area.reb_small_retail_rent_area_id = metric.reb_small_retail_rent_area_id
        WHERE area.source_area_key = ?
        """,
        BigDecimal.class,
        sourceAreaKey);
  }

  private String sourceFileName() {
    return jdbcTemplate.queryForObject(
        "SELECT source_file_name FROM reb_small_retail_rent_metrics", String.class);
  }
}
