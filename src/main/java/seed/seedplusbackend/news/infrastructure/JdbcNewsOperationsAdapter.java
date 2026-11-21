package seed.seedplusbackend.news.infrastructure;

import java.time.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import seed.seedplusbackend.news.application.port.NewsOperationsPort;
import seed.seedplusbackend.news.domain.NewsPolicy;

@Repository
@RequiredArgsConstructor
public class JdbcNewsOperationsAdapter implements NewsOperationsPort {
  private final JdbcTemplate jdbc;

  @Override
  @Transactional
  public boolean recordView(long id, String hash, LocalDate date, OffsetDateTime now) {
    int inserted =
        jdbc.update(
            """
       INSERT INTO news_view_logs(article_id,viewer_hash,view_date,viewed_at) VALUES (?,?,?,?)
       ON CONFLICT DO NOTHING
       """,
            id,
            hash,
            date,
            now);
    if (inserted == 1)
      jdbc.update("UPDATE news_articles SET view_count=view_count+1 WHERE news_article_id=?", id);
    return inserted == 1;
  }

  @Override
  @Transactional
  public void createDigest(LocalDate date) {
    jdbc.update("DELETE FROM news_digests WHERE digest_date=?", date);
    jdbc.update(
        """
       INSERT INTO news_digests(digest_date,article_id,position)
       SELECT ?,news_article_id,row_number() OVER(ORDER BY category,category_position) FROM (
         SELECT a.news_article_id,a.category,
           row_number() OVER(PARTITION BY a.category ORDER BY count(v.article_id) DESC,a.published_at DESC,a.news_article_id DESC) AS category_position
         FROM news_articles a LEFT JOIN news_view_logs v ON v.article_id=a.news_article_id AND v.view_date=?
         WHERE a.active AND a.published_at>=? AND a.published_at<?
         GROUP BY a.news_article_id
       ) ranked WHERE category_position<=3
       """,
        date,
        date.minusDays(1),
        date.minusDays(1).atStartOfDay(NewsPolicy.ZONE).toOffsetDateTime(),
        date.atStartOfDay(NewsPolicy.ZONE).toOffsetDateTime());
  }

  @Override
  public void pruneViews(LocalDate before) {
    jdbc.update("DELETE FROM news_view_logs WHERE view_date<?", before);
  }
}
