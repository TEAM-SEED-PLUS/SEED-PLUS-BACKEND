package seed.seedplusbackend.commercial.application.result;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class KosisBusinessCountItemTest {

  @Test
  void mapsActualKosisItemIds() {
    assertThat(KosisBusinessCountItem.fromItemId("T01")).contains(KosisBusinessCountItem.ACTIVE);
    assertThat(KosisBusinessCountItem.fromItemId("T02")).contains(KosisBusinessCountItem.NEW);
    assertThat(KosisBusinessCountItem.fromItemId("T03")).contains(KosisBusinessCountItem.CLOSED);
  }

  @Test
  void doesNotMapUnknownItemId() {
    assertThat(KosisBusinessCountItem.fromItemId("T99")).isEmpty();
  }
}
