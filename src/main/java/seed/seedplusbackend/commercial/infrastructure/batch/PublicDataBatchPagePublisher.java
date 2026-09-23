package seed.seedplusbackend.commercial.infrastructure.batch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import seed.seedplusbackend.commercial.application.provider.CommercialDataType;
import seed.seedplusbackend.commercial.application.result.*;
import seed.seedplusbackend.commercial.infrastructure.repository.*;

@RequiredArgsConstructor
public class PublicDataBatchPagePublisher {

  private final ObjectMapper mapper;
  private final CommercialEstimatedSalesJdbcRepository sales;
  private final SmallBusinessStoreJdbcRepository stores;
  private final KosisBusinessCountJdbcRepository counts;
  private final KosisBusinessSurvivalJdbcRepository survival;
  private final SeoulSdotFootTrafficJdbcRepository traffic;
  private final SeoulRealtimeCityPopulationJdbcRepository city;

  // 수집용 Port 대신 실제 저장소를 호출해 임시 저장으로 다시 라우팅되지 않게 한다.
  public long publish(CommercialDataType type, String target, String json) {
    return switch (type) {
      case SEOUL_ESTIMATED_SALES -> {
        var rows = rows(json, CommercialEstimatedSalesRowResult.class);
        sales.upsertAll(rows);
        yield rows.size();
      }
      case SMALL_BUSINESS_STORE -> {
        var rows = rows(json, SmallBusinessStoreRowResult.class);
        stores.upsertAll(target, rows);
        yield rows.size();
      }
      case KOSIS_BUSINESS_COUNT -> {
        var rows = rows(json, KosisBusinessCountRowResult.class);
        counts.upsertAll(rows);
        yield rows.size();
      }
      case KOSIS_BUSINESS_SURVIVAL_RATE -> {
        var rows = rows(json, KosisBusinessSurvivalRowResult.class);
        survival.upsertAll(rows);
        yield rows.size();
      }
      case SEOUL_SDOT_FOOT_TRAFFIC -> {
        var rows = rows(json, SeoulSdotFootTrafficRowResult.class);
        traffic.upsertAll(rows);
        yield rows.size();
      }
      case SEOUL_REALTIME_CITY_POPULATION -> {
        var rows = rows(json, SeoulRealtimeCityPopulationResult.class);
        rows.forEach(city::upsert);
        yield rows.size();
      }
      case REB_SMALL_RETAIL_RENT -> throw new IllegalArgumentException("임대료는 수동 적재 대상입니다.");
    };
  }

  private <T> List<T> rows(String json, Class<T> rowType) {
    try {
      return mapper.readValue(
          json, mapper.getTypeFactory().constructCollectionType(List.class, rowType));
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("임시 저장 데이터를 읽지 못했습니다.", exception);
    }
  }
}
