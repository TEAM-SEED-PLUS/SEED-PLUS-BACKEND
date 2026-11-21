package seed.seedplusbackend.news.infrastructure;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import org.junit.jupiter.api.*;
import org.springframework.http.*;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.news.domain.NewsSource;

@DisplayName("중기부 RSS 어댑터")
class MssRssClientTest {
  @Test
  @DisplayName("실제 형식의 CDATA·숫자 날짜·상수 id를 읽고 본문은 버린다")
  void parseBothSources() {
    for (int board : new int[] {86, 310}) {
      String xml =
          """
       <?xml version="1.0" encoding="UTF-8"?>
       <rss version="2.0"><channel><title>중기부</title>
       <item><title><![CDATA[소상공인 창업 지원]]></title>
       <link><![CDATA[https://www.mss.go.kr/site/smba/ex/bbs/View.do?cbIdx=%d&bcIdx=123]]></link>
       <pubDate><![CDATA[20261120090000]]></pubDate><id>%d</id><description>저장하지 않는 본문</description></item>
       <item><title>잘못된 항목</title></item></channel></rss>
       """
              .formatted(board, board);
      var result = MssRssClient.parse(xml);
      assertThat(result).hasSize(2);
      assertThat(result.getFirst().publishedText()).isEqualTo("20261120090000");
      assertThat(result.getFirst().url()).endsWith("bcIdx=123");
      assertThat(result.toString()).doesNotContain("저장하지 않는 본문");
      assertThat(result.get(1).url()).isNull();
    }
  }

  @Test
  @DisplayName("외부 엔티티와 RSS가 아닌 응답을 거부한다")
  void rejectXml() {
    assertThatThrownBy(
            () ->
                MssRssClient.parse(
                    """
     <!DOCTYPE rss [<!ENTITY xxe SYSTEM "file:///etc/passwd">]>
     <rss><channel><item><title>&xxe;</title></item></channel></rss>
     """))
        .isInstanceOf(ApplicationException.class);
    assertThatThrownBy(() -> MssRssClient.parse("<html>maintenance</html>"))
        .isInstanceOf(ApplicationException.class);
    assertThatThrownBy(() -> MssRssClient.parse("<rss><item>"))
        .isInstanceOf(ApplicationException.class);
  }

  @Test
  @DisplayName("지정 RSS 경로만 호출하고 오류 응답은 수집 실패로 전달한다")
  void httpContract() {
    var builder = RestClient.builder();
    var server = MockRestServiceServer.bindTo(builder).build();
    var client = new MssRssClient(builder, "https://www.mss.go.kr");
    server
        .expect(requestTo("https://www.mss.go.kr/rss/smba/board/310.do"))
        .andRespond(withSuccess("<rss><channel/></rss>", MediaType.APPLICATION_XML));
    assertThat(client.fetch(NewsSource.MSS_ANNOUNCEMENT)).isEmpty();
    server.verify();
    server.reset();
    server
        .expect(requestTo("https://www.mss.go.kr/rss/smba/board/86.do"))
        .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));
    assertThatThrownBy(() -> client.fetch(NewsSource.MSS_PRESS))
        .isInstanceOf(ApplicationException.class);
    server.verify();
  }
}
