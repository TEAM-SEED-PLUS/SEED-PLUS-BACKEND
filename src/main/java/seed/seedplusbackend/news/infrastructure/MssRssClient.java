package seed.seedplusbackend.news.infrastructure;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.xml.stream.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import seed.seedplusbackend.global.error.*;
import seed.seedplusbackend.news.application.port.NewsFeedPort;
import seed.seedplusbackend.news.domain.NewsSource;

@Component
public class MssRssClient implements NewsFeedPort {
  private static final int MAX_BYTES = 2 * 1024 * 1024;
  private final RestClient client;

  public MssRssClient(
      @Qualifier("externalRestClientBuilder") RestClient.Builder builder,
      @Value("${news.rss.base-url:https://www.mss.go.kr}") String baseUrl) {
    this.client = builder.clone().baseUrl(baseUrl).build();
  }

  @Override
  public List<RawItem> fetch(NewsSource source) {
    if (!source.rss()) throw new ApplicationException(ErrorCode.INVALID_NEWS_ITEM);
    try {
      String xml =
          client
              .get()
              .uri("/rss/smba/board/" + source.boardId() + ".do")
              .exchange(
                  (request, response) -> {
                    if (!response.getStatusCode().is2xxSuccessful())
                      throw new ApplicationException(ErrorCode.NEWS_SOURCE_FAILED);
                    byte[] bytes = response.getBody().readNBytes(MAX_BYTES + 1);
                    if (bytes.length > MAX_BYTES)
                      throw new ApplicationException(ErrorCode.NEWS_SOURCE_FAILED);
                    return new String(bytes, StandardCharsets.UTF_8);
                  });
      return parse(xml);
    } catch (ApplicationException e) {
      throw e;
    } catch (Exception e) {
      throw new ApplicationException(ErrorCode.NEWS_SOURCE_FAILED, e);
    }
  }

  public static List<RawItem> parse(String xml) {
    XMLInputFactory factory = XMLInputFactory.newFactory();
    factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
    factory.setProperty("javax.xml.stream.isSupportingExternalEntities", false);
    factory.setXMLResolver(
        (publicId, systemId, baseUri, namespace) -> {
          throw new XMLStreamException("External entities disabled");
        });
    var items = new ArrayList<RawItem>();
    XMLStreamReader reader = null;
    try {
      reader = factory.createXMLStreamReader(new StringReader(xml));
      boolean item = false;
      boolean rss = false;
      String title = null, url = null, date = null;
      while (reader.hasNext()) {
        int event = reader.next();
        if (event == XMLStreamConstants.DTD) throw new XMLStreamException("DTD disabled");
        if (event == XMLStreamConstants.START_ELEMENT) {
          String name = reader.getLocalName();
          if (name.equals("rss")) rss = true;
          if (name.equals("item")) {
            item = true;
            title = null;
            url = null;
            date = null;
          } else if (item && name.equals("title")) title = reader.getElementText();
          else if (item && name.equals("link")) url = reader.getElementText();
          else if (item && name.equals("pubDate")) date = reader.getElementText();
        } else if (event == XMLStreamConstants.END_ELEMENT
            && reader.getLocalName().equals("item")) {
          items.add(new RawItem(title, url, date));
          item = false;
          if (items.size() > 1000) throw new XMLStreamException("Too many RSS items");
        }
      }
      if (!rss) throw new XMLStreamException("RSS root missing");
      return List.copyOf(items);
    } catch (XMLStreamException | RuntimeException e) {
      throw new ApplicationException(ErrorCode.NEWS_SOURCE_FAILED, e);
    } finally {
      if (reader != null)
        try {
          reader.close();
        } catch (XMLStreamException ignored) {
        }
    }
  }
}
