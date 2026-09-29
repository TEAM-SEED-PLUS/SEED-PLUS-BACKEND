package seed.seedplusbackend.commercial.infrastructure.batch;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SeoulCollectionTargets {
  private final JdbcTemplate jdbcTemplate;

  public List<String> districts() {
    return jdbcTemplate.queryForList(
        """
        SELECT DISTINCT code FROM regions
        WHERE sido = '서울특별시' AND code_type = 'SIGUNGU'
          AND code ~ '^11[0-9]{3}$'
        ORDER BY code
        """,
        String.class);
  }

  public List<String> cityAreas() {
    return jdbcTemplate.queryForList(
        """
        SELECT area_name FROM seoul_city_collection_targets
        WHERE enabled = TRUE ORDER BY area_name
        """,
        String.class);
  }
}
