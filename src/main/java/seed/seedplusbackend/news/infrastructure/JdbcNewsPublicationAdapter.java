package seed.seedplusbackend.news.infrastructure;

import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import seed.seedplusbackend.news.application.port.NewsPublicationPort;
import seed.seedplusbackend.news.domain.*;

@Repository
@RequiredArgsConstructor
public class JdbcNewsPublicationAdapter implements NewsPublicationPort {
  private final JdbcTemplate jdbc;

  @Override
  @Transactional
  public long publish(NewsItem item) {
    Long id =
        jdbc.queryForObject(
            """
     INSERT INTO news_articles(source,source_key,content_type,display_mode,category,title,normalized_title,
       summary,body,original_url,tags,published_at)
     VALUES (?,?,?,?,?,?,?,?,?,?,?,?)
     ON CONFLICT(source,source_key) DO UPDATE SET title=excluded.title, normalized_title=excluded.normalized_title,
       category=excluded.category,summary=excluded.summary,body=excluded.body,original_url=excluded.original_url,
       tags=excluded.tags,active=true,updated_at=now(),
       published_at=CASE WHEN news_articles.content_type='INSIGHT' THEN news_articles.published_at ELSE excluded.published_at END
     RETURNING news_article_id
     """,
            Long.class,
            item.source().name(),
            item.sourceKey(),
            item.contentType(),
            item.displayMode(),
            item.category().name(),
            item.title(),
            item.normalizedTitle(),
            item.summary(),
            item.body(),
            item.originalUrl(),
            item.tags(),
            item.publishedAt());
    var regions = new LinkedHashSet<>(item.regionIds());
    var industries = new LinkedHashSet<>(item.industryIds());
    if (item.source().rss()) {
      regions.addAll(
          jdbc.queryForList(
              """
       SELECT region_id FROM regions WHERE code_type='SIGUNGU' AND length(sigungu)>1 AND strpos(?,sigungu)>0
       """,
              Long.class,
              item.title()));
      industries.addAll(
          jdbc.queryForList(
              """
       SELECT industry_id FROM industries WHERE status='ACTIVE' AND length(name)>1 AND strpos(?,name)>0
       """,
              Long.class,
              item.title()));
    }
    jdbc.update("DELETE FROM news_article_regions WHERE article_id=?", id);
    for (Long region : regions)
      jdbc.update("INSERT INTO news_article_regions VALUES (?,?)", id, region);
    jdbc.update("DELETE FROM news_article_industries WHERE article_id=?", id);
    for (Long industry : industries)
      jdbc.update("INSERT INTO news_article_industries VALUES (?,?)", id, industry);
    return id;
  }

  @Override
  public void hide(NewsSource source, String key) {
    jdbc.update(
        "UPDATE news_articles SET active=false,updated_at=now() WHERE source=? AND source_key=?",
        source.name(),
        key);
  }
}
