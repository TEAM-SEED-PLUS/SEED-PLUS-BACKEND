package seed.seedplusbackend.commercial.infrastructure.repository;

import java.sql.Types;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import seed.seedplusbackend.commercial.application.port.SeoulRealtimeCityPopulationStorePort;
import seed.seedplusbackend.commercial.application.result.SeoulRealtimeCityPopulationResult;

@Repository
@RequiredArgsConstructor
public class SeoulRealtimeCityPopulationJdbcRepository
    implements SeoulRealtimeCityPopulationStorePort {

  private static final String UPSERT_SQL =
      """
      INSERT INTO seoul_realtime_city_populations (
        area_code, area_name, congestion_level, congestion_message,
        population_min, population_max, estimated_population,
        male_population_rate, female_population_rate,
        population_rate_0, population_rate_10, population_rate_20,
        population_rate_30, population_rate_40, population_rate_50,
        population_rate_60, population_rate_70,
        resident_population_rate, non_resident_population_rate,
        replacement_used, population_time, collected_at, created_at, updated_at
      ) VALUES (
        ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now(), now(), now()
      )
      ON CONFLICT (area_code, population_time)
      DO UPDATE SET
        area_name = EXCLUDED.area_name,
        congestion_level = EXCLUDED.congestion_level,
        congestion_message = EXCLUDED.congestion_message,
        population_min = EXCLUDED.population_min,
        population_max = EXCLUDED.population_max,
        estimated_population = EXCLUDED.estimated_population,
        male_population_rate = EXCLUDED.male_population_rate,
        female_population_rate = EXCLUDED.female_population_rate,
        population_rate_0 = EXCLUDED.population_rate_0,
        population_rate_10 = EXCLUDED.population_rate_10,
        population_rate_20 = EXCLUDED.population_rate_20,
        population_rate_30 = EXCLUDED.population_rate_30,
        population_rate_40 = EXCLUDED.population_rate_40,
        population_rate_50 = EXCLUDED.population_rate_50,
        population_rate_60 = EXCLUDED.population_rate_60,
        population_rate_70 = EXCLUDED.population_rate_70,
        resident_population_rate = EXCLUDED.resident_population_rate,
        non_resident_population_rate = EXCLUDED.non_resident_population_rate,
        replacement_used = EXCLUDED.replacement_used,
        collected_at = now(),
        updated_at = now()
      """;

  private final JdbcTemplate jdbcTemplate;

  @Override
  @Transactional
  public void upsert(SeoulRealtimeCityPopulationResult result) {
    jdbcTemplate.update(
        connection -> {
          var statement = connection.prepareStatement(UPSERT_SQL);
          int index = 1;
          statement.setString(index++, result.areaCode());
          statement.setString(index++, result.areaName());
          statement.setString(index++, result.congestionLevel());
          statement.setString(index++, result.congestionMessage());
          statement.setLong(index++, result.populationMin());
          statement.setLong(index++, result.populationMax());
          statement.setLong(index++, result.estimatedPopulation());
          setDecimal(statement, index++, result.malePopulationRate());
          setDecimal(statement, index++, result.femalePopulationRate());
          setDecimal(statement, index++, result.populationRate0());
          setDecimal(statement, index++, result.populationRate10());
          setDecimal(statement, index++, result.populationRate20());
          setDecimal(statement, index++, result.populationRate30());
          setDecimal(statement, index++, result.populationRate40());
          setDecimal(statement, index++, result.populationRate50());
          setDecimal(statement, index++, result.populationRate60());
          setDecimal(statement, index++, result.populationRate70());
          setDecimal(statement, index++, result.residentPopulationRate());
          setDecimal(statement, index++, result.nonResidentPopulationRate());
          statement.setBoolean(index++, result.replacementUsed());
          statement.setObject(index, result.populationTime());
          return statement;
        });
  }

  private void setDecimal(
      java.sql.PreparedStatement statement, int index, java.math.BigDecimal value)
      throws java.sql.SQLException {
    if (value == null) {
      statement.setNull(index, Types.NUMERIC);
    } else {
      statement.setBigDecimal(index, value);
    }
  }
}
