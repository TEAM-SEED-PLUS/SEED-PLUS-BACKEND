package seed.seedplusbackend.commercial.infrastructure.batch;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import seed.seedplusbackend.commercial.application.port.*;
import seed.seedplusbackend.commercial.application.provider.CommercialDataType;
import seed.seedplusbackend.commercial.infrastructure.repository.*;

public class PublicDataBatchStoreRouting {

  @Bean
  @Primary
  CommercialEstimatedSalesStorePort stagedSales(
      PublicDataBatchStaging staging, CommercialEstimatedSalesJdbcRepository repository) {
    return rows -> {
      if (!staging.capture(CommercialDataType.SEOUL_ESTIMATED_SALES, null, rows))
        repository.upsertAll(rows);
    };
  }

  @Bean
  @Primary
  SmallBusinessStoreStorePort stagedStores(
      PublicDataBatchStaging staging, SmallBusinessStoreJdbcRepository repository) {
    return (target, rows) -> {
      if (!staging.capture(CommercialDataType.SMALL_BUSINESS_STORE, target, rows))
        repository.upsertAll(target, rows);
    };
  }

  @Bean
  @Primary
  KosisBusinessCountStorePort stagedCounts(
      PublicDataBatchStaging staging, KosisBusinessCountJdbcRepository repository) {
    return rows -> {
      if (!staging.capture(CommercialDataType.KOSIS_BUSINESS_COUNT, null, rows))
        repository.upsertAll(rows);
    };
  }

  @Bean
  @Primary
  KosisBusinessSurvivalStorePort stagedSurvival(
      PublicDataBatchStaging staging, KosisBusinessSurvivalJdbcRepository repository) {
    return rows -> {
      if (!staging.capture(CommercialDataType.KOSIS_BUSINESS_SURVIVAL_RATE, null, rows))
        repository.upsertAll(rows);
    };
  }

  @Bean
  @Primary
  SeoulSdotFootTrafficStorePort stagedTraffic(
      PublicDataBatchStaging staging, SeoulSdotFootTrafficJdbcRepository repository) {
    return rows -> {
      if (!staging.capture(CommercialDataType.SEOUL_SDOT_FOOT_TRAFFIC, null, rows))
        repository.upsertAll(rows);
    };
  }

  @Bean
  @Primary
  SeoulRealtimeCityPopulationStorePort stagedCity(
      PublicDataBatchStaging staging, SeoulRealtimeCityPopulationJdbcRepository repository) {
    return row -> {
      if (!staging.capture(CommercialDataType.SEOUL_REALTIME_CITY_POPULATION, null, List.of(row)))
        repository.upsert(row);
    };
  }
}
