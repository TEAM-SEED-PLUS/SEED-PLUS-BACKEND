package seed.seedplusbackend.news;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import seed.seedplusbackend.commercial.infrastructure.batch.PublicDataBatchLauncher;
import seed.seedplusbackend.news.application.port.NewsFeedPort;
import seed.seedplusbackend.news.domain.NewsSource;
import seed.seedplusbackend.news.infrastructure.NewsJobRunner;
import seed.seedplusbackend.support.AbstractPostgresContainerTest;

@SpringBootTest(
    properties = {
      "public-data.batch.enabled=true",
      "public-data.collection.crons.SEOUL_SDOT_FOOT_TRAFFIC=-",
      "public-data.collection.crons.SEOUL_REALTIME_CITY_POPULATION=-",
      "public-data.collection.crons.SEOUL_ESTIMATED_SALES=-",
      "public-data.collection.crons.SMALL_BUSINESS_STORE=-",
      "public-data.collection.crons.KOSIS_BUSINESS_COUNT=-",
      "public-data.collection.crons.KOSIS_BUSINESS_SURVIVAL_RATE=-",
      "news.batch.enabled=true",
      "news.batch.press-cron=-",
      "news.batch.announcement-cron=-",
      "news.batch.insight-cron=-",
      "news.batch.digest-cron=-"
    })
@DisplayName("뉴스·공공데이터 배치 구성 호환")
class NewsBatchConfigurationTest extends AbstractPostgresContainerTest {
  @Autowired ApplicationContext context;
  @Autowired NewsJobRunner runner;
  @MockitoBean NewsFeedPort feed;
  @MockitoBean PublicDataBatchLauncher publicData;

  @Test
  @DisplayName("두 배치와 시계가 함께 등록되고 비활성 cron은 외부 수집을 실행하지 않는다")
  void coexist() {
    assertThat(context.containsBean("newsPipelineJob")).isTrue();
    assertThat(context.containsBean("publicDataCollectionJob")).isTrue();
    assertThat(context.containsBean("newsClock")).isTrue();
    assertThat(context.containsBean("publicDataBatchClock")).isTrue();
    verifyNoInteractions(feed, publicData);
    when(feed.fetch(NewsSource.MSS_ANNOUNCEMENT)).thenReturn(List.of());
    assertThat(runner.run("MSS_ANNOUNCEMENT").getStatus())
        .isEqualTo(org.springframework.batch.core.BatchStatus.COMPLETED);
  }
}
