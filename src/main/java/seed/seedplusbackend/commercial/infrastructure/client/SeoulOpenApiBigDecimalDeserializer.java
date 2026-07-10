package seed.seedplusbackend.commercial.infrastructure.client;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.math.BigDecimal;

public class SeoulOpenApiBigDecimalDeserializer extends JsonDeserializer<BigDecimal> {

  @Override
  public BigDecimal deserialize(JsonParser parser, DeserializationContext context)
      throws IOException {
    String value = parser.getValueAsString();

    if (value == null || value.isBlank() || "-".equals(value)) {
      return BigDecimal.ZERO;
    }

    return new BigDecimal(value.replace(",", ""));
  }
}
