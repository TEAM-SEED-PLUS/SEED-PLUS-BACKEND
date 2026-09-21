package seed.seedplusbackend.commercial.infrastructure.batch;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import seed.seedplusbackend.support.AbstractPostgresContainerTest;
import seed.seedplusbackend.support.RepositoryTest;

@RepositoryTest
class SeoulCollectionTargetsTest extends AbstractPostgresContainerTest {
  @Autowired JdbcTemplate jdbcTemplate;

  @Test
  @DisplayName("기존 서울 시군구 기준정보만 읽고 다른 시도와 동은 제외한다")
  void readsSeoulDistrictReferenceData() {
    jdbcTemplate.update(
        """
        INSERT INTO regions(sido, sigungu, dong, code, code_type)
        VALUES ('부산광역시', '테스트구', '', '26999', 'SIGUNGU')
        """);
    var targets = new SeoulCollectionTargets(jdbcTemplate);
    assertThat(targets.districts())
        .hasSize(25)
        .doesNotHaveDuplicates()
        .contains("11680")
        .doesNotContain("26999", "1168010100");
  }

  @Test
  @DisplayName("도시데이터 대상은 DB 기준정보와 활성화 상태 변경을 반영한다")
  void readsEnabledCityTargetsFromDatabase() {
    var targets = new SeoulCollectionTargets(jdbcTemplate);
    assertThat(targets.cityAreas()).hasSize(121).doesNotHaveDuplicates();
    jdbcTemplate.update(
        "UPDATE seoul_city_collection_targets SET enabled = FALSE WHERE area_name = ?", "홍제폭포");
    assertThat(targets.cityAreas()).hasSize(120).doesNotContain("홍제폭포");
  }
}
