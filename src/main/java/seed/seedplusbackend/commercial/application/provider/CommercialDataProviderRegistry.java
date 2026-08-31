package seed.seedplusbackend.commercial.application.provider;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class CommercialDataProviderRegistry {

  private final Map<CommercialDataType, CommercialDataProvider> providers;

  public CommercialDataProviderRegistry(List<CommercialDataProvider> providers) {
    this.providers = new EnumMap<>(CommercialDataType.class);
    for (CommercialDataProvider provider : providers) {
      CommercialDataProvider duplicated = this.providers.put(provider.supports(), provider);
      if (duplicated != null) {
        throw new IllegalStateException("같은 데이터 유형의 Provider가 중복 등록되었습니다.");
      }
    }
  }

  public CommercialDataProvider get(CommercialDataType dataType) {
    CommercialDataProvider provider = providers.get(dataType);
    if (provider == null) {
      throw new IllegalArgumentException("지원하지 않는 데이터 유형입니다: " + dataType);
    }
    return provider;
  }
}
