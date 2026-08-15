package seed.seedplusbackend.commercial.application.result;

import java.util.Arrays;
import java.util.Optional;

public enum KosisBusinessCountItem {
  ACTIVE("T01"),
  NEW("T02"),
  CLOSED("T03");

  private final String itemId;

  KosisBusinessCountItem(String itemId) {
    this.itemId = itemId;
  }

  public String itemId() {
    return itemId;
  }

  public static Optional<KosisBusinessCountItem> fromItemId(String itemId) {
    return Arrays.stream(values()).filter(item -> item.itemId.equals(itemId)).findFirst();
  }
}
