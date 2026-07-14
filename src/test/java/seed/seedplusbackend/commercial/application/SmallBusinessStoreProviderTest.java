package seed.seedplusbackend.commercial.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import seed.seedplusbackend.commercial.application.command.SmallBusinessStoreCollectCommand;
import seed.seedplusbackend.commercial.application.port.SmallBusinessStoreClientPort;
import seed.seedplusbackend.commercial.application.port.SmallBusinessStoreStorePort;
import seed.seedplusbackend.commercial.application.provider.SmallBusinessStoreProvider;
import seed.seedplusbackend.commercial.application.result.SmallBusinessStorePageResult;
import seed.seedplusbackend.commercial.application.result.SmallBusinessStoreRowResult;
import seed.seedplusbackend.commercial.infrastructure.client.SmallBusinessStoreOpenApiProperties;

@ExtendWith(MockitoExtension.class)
@DisplayName("소상공인 상가정보 Provider")
class SmallBusinessStoreProviderTest {

  @Mock private SmallBusinessStoreClientPort clientPort;
  @Mock private SmallBusinessStoreStorePort storePort;

  @Test
  @DisplayName("모든 페이지를 조회하고 저장한다")
  void collect_savesEveryPage() {
    SmallBusinessStoreCollectCommand command =
        new SmallBusinessStoreCollectCommand("9151", "Q", "Q12", "Q12A01", false);
    SmallBusinessStoreRowResult first = mock(SmallBusinessStoreRowResult.class);
    SmallBusinessStoreRowResult second = mock(SmallBusinessStoreRowResult.class);
    SmallBusinessStoreRowResult third = mock(SmallBusinessStoreRowResult.class);
    org.mockito.Mockito.doReturn(new SmallBusinessStorePageResult(3, List.of(first, second)))
        .when(clientPort)
        .fetch(command, 1, 2);
    org.mockito.Mockito.doReturn(new SmallBusinessStorePageResult(3, List.of(third)))
        .when(clientPort)
        .fetch(command, 2, 2);
    SmallBusinessStoreProvider provider =
        new SmallBusinessStoreProvider(
            clientPort,
            storePort,
            new SmallBusinessStoreOpenApiProperties(
                "key", "http://localhost", "storeListInArea", "json", 2));
    List<Long> fetchedCounts = new ArrayList<>();

    provider.collect(command, (total, fetched, cursor) -> fetchedCounts.add(fetched));

    assertThat(fetchedCounts).containsExactly(2L, 3L);
    verify(storePort).upsertAll("9151", List.of(first, second));
    verify(storePort).upsertAll("9151", List.of(third));
  }
}
