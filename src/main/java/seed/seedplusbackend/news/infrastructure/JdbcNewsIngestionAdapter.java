package seed.seedplusbackend.news.infrastructure;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.OffsetDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import seed.seedplusbackend.global.error.*;
import seed.seedplusbackend.news.application.port.*;
import seed.seedplusbackend.news.domain.*;

@Repository
@RequiredArgsConstructor
public class JdbcNewsIngestionAdapter implements NewsIngestionPort {
  private final JdbcTemplate jdbc;

  @Override
  public void stage(NewsSource source, List<NewsFeedPort.RawItem> items) {
    for (var raw : items) {
      boolean oversize =
          tooLong(raw.title(), 1000)
              || tooLong(raw.url(), 2000)
              || tooLong(raw.publishedText(), 100);
      String key =
          hash(raw.url() == null || raw.url().isBlank() ? String.valueOf(raw) : raw.url().trim());
      jdbc.update(
          """
       INSERT INTO news_ingestion_items(source,item_key,title,original_url,published_text,status,error_code)
       VALUES (?,?,?,?,?,?,?)
       ON CONFLICT(source,item_key) DO UPDATE SET title=excluded.title,original_url=excluded.original_url,
         published_text=excluded.published_text,status=excluded.status,error_code=excluded.error_code,
         attempts=0,next_attempt_at=now(),updated_at=now()
       WHERE news_ingestion_items.title IS DISTINCT FROM excluded.title
         OR news_ingestion_items.original_url IS DISTINCT FROM excluded.original_url
         OR news_ingestion_items.published_text IS DISTINCT FROM excluded.published_text
       """,
          source.name(),
          key,
          clip(raw.title(), 1000),
          clip(raw.url(), 2000),
          clip(raw.publishedText(), 100),
          oversize ? "INVALID" : "PENDING",
          oversize ? "FIELD_TOO_LONG" : null);
    }
  }

  @Override
  public List<Pending> pending(NewsSource source) {
    return jdbc.query(
        """
     SELECT * FROM news_ingestion_items WHERE source=? AND status IN ('PENDING','RETRY')
       AND next_attempt_at<=now() ORDER BY item_id LIMIT 100
     """,
        (rs, n) ->
            new Pending(
                rs.getLong("item_id"),
                rs.getString("title"),
                rs.getString("original_url"),
                rs.getString("published_text"),
                rs.getInt("attempts"),
                rs.getObject("created_at", OffsetDateTime.class)),
        source.name());
  }

  @Override
  public void complete(long id, String status, Long articleId, Long duplicateId) {
    jdbc.update(
        """
     UPDATE news_ingestion_items SET status=?,article_id=?,duplicate_candidate_id=?,
       error_code=CASE WHEN ?='INVALID' THEN 'INVALID_NEWS_ITEM' ELSE NULL END,updated_at=now() WHERE item_id=?
     """,
        status,
        articleId,
        duplicateId,
        status,
        id);
  }

  @Override
  public void fail(long id, String code) {
    jdbc.update(
        """
     UPDATE news_ingestion_items SET attempts=attempts+1,
       status=CASE WHEN attempts>=3 THEN 'FAILED' ELSE 'RETRY' END,error_code=?,
       next_attempt_at=now()+ CASE attempts WHEN 0 THEN interval '1 minute'
         WHEN 1 THEN interval '5 minutes' ELSE interval '15 minutes' END,updated_at=now()
     WHERE item_id=? AND attempts<4
     """,
        code,
        id);
  }

  @Override
  public Long duplicateCandidate(NewsItem item, long id) {
    var ids =
        jdbc.queryForList(
            """
     SELECT news_article_id FROM news_articles WHERE normalized_title=? AND news_article_id<>? AND active
     ORDER BY news_article_id LIMIT 1
     """,
            Long.class,
            item.normalizedTitle(),
            id);
    return ids.isEmpty() ? null : ids.getFirst();
  }

  @Override
  public void sourceSucceeded(NewsSource source) {
    jdbc.update(
        """
     INSERT INTO news_source_states(source,last_success_at) VALUES (?,now())
     ON CONFLICT(source) DO UPDATE SET last_success_at=excluded.last_success_at
     """,
        source.name());
  }

  @Override
  public void sourceFailed(NewsSource source) {
    jdbc.update(
        """
     INSERT INTO news_source_states(source,last_failure_at) VALUES (?,now())
     ON CONFLICT(source) DO UPDATE SET last_failure_at=excluded.last_failure_at
     """,
        source.name());
  }

  private static boolean tooLong(String v, int n) {
    return v != null && v.length() > n;
  }

  private static String clip(String v, int n) {
    return v == null ? null : v.substring(0, Math.min(v.length(), n));
  }

  private static String hash(String v) {
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256").digest(v.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new ApplicationException(ErrorCode.INTERNAL_SERVER_ERROR, e);
    }
  }
}
