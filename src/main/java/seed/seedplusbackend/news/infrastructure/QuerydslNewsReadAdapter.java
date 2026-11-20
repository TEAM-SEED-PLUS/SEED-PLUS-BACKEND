package seed.seedplusbackend.news.infrastructure;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import seed.seedplusbackend.industry.domain.entity.QIndustry;
import seed.seedplusbackend.news.application.port.NewsReadPort;
import seed.seedplusbackend.news.application.query.NewsSearchQuery;
import seed.seedplusbackend.news.domain.NewsPolicy;
import seed.seedplusbackend.news.domain.entity.*;
import seed.seedplusbackend.region.domain.entity.QRegion;

@Repository
@RequiredArgsConstructor
public class QuerydslNewsReadAdapter implements NewsReadPort {
  private final JPAQueryFactory queries;
  private final JdbcTemplate jdbc;

  @Override
  public Page<NewsArticle> search(NewsSearchQuery query, OffsetDateTime now) {
    var a = QNewsArticle.newsArticle;
    var tagged = new QNewsArticle("taggedNews");
    var region = new QRegion("newsRegion");
    var industry = new QIndustry("newsIndustry");
    var parent = new QIndustry("newsIndustryParent");
    var grandparent = new QIndustry("newsIndustryGrandparent");
    var where = new BooleanBuilder(a.active.isTrue().and(a.publishedAt.loe(now)));
    if (query.category() != null)
      where.and(
          a.category.eq(seed.seedplusbackend.news.domain.NewsCategory.valueOf(query.category())));
    if (query.from() != null)
      where.and(a.publishedAt.goe(query.from().atStartOfDay(NewsPolicy.ZONE).toOffsetDateTime()));
    if (query.to() != null)
      where.and(
          a.publishedAt.lt(
              query.to().plusDays(1).atStartOfDay(NewsPolicy.ZONE).toOffsetDateTime()));
    if (!query.regionIds().isEmpty())
      where.and(
          JPAExpressions.selectOne()
              .from(tagged)
              .join(tagged.regions, region)
              .where(tagged.id.eq(a.id), region.id.in(query.regionIds()))
              .exists());
    if (!query.industryIds().isEmpty())
      where.and(
          JPAExpressions.selectOne()
              .from(tagged)
              .join(tagged.industries, industry)
              .leftJoin(industry.parentIndustry, parent)
              .leftJoin(parent.parentIndustry, grandparent)
              .where(
                  tagged.id.eq(a.id),
                  industry
                      .id
                      .in(query.industryIds())
                      .or(parent.id.in(query.industryIds()))
                      .or(grandparent.id.in(query.industryIds())))
              .exists());
    if (query.keyword() != null && !query.keyword().isBlank()) {
      String k = query.keyword().trim();
      where.and(
          a.title
              .containsIgnoreCase(k)
              .or(a.summary.containsIgnoreCase(k))
              .or(a.tags.containsIgnoreCase(k))
              .or(
                  JPAExpressions.selectOne()
                      .from(tagged)
                      .join(tagged.regions, region)
                      .where(
                          tagged.id.eq(a.id),
                          region
                              .sigungu
                              .containsIgnoreCase(k)
                              .or(region.sido.containsIgnoreCase(k)))
                      .exists())
              .or(
                  JPAExpressions.selectOne()
                      .from(tagged)
                      .join(tagged.industries, industry)
                      .where(tagged.id.eq(a.id), industry.name.containsIgnoreCase(k))
                      .exists()));
    }
    var items =
        queries
            .selectFrom(a)
            .where(where)
            .orderBy(a.publishedAt.desc(), a.id.desc())
            .offset(query.pageable().getOffset())
            .limit(query.pageable().getPageSize())
            .fetch();
    Long count = queries.select(a.count()).from(a).where(where).fetchOne();
    return new PageImpl<>(items, query.pageable(), count == null ? 0 : count);
  }

  @Override
  public Optional<NewsArticle> find(long id, OffsetDateTime now) {
    var a = QNewsArticle.newsArticle;
    return Optional.ofNullable(
        queries
            .selectFrom(a)
            .where(a.id.eq(id), a.active.isTrue(), a.publishedAt.loe(now))
            .fetchOne());
  }

  @Override
  public List<NewsArticle> popular(LocalDate from, LocalDate to, OffsetDateTime now) {
    var ids =
        jdbc.queryForList(
            """
       SELECT a.news_article_id FROM news_articles a
       LEFT JOIN (SELECT article_id, count(*) AS views FROM news_view_logs
         WHERE view_date BETWEEN ? AND ? GROUP BY article_id) v ON v.article_id=a.news_article_id
       WHERE a.active AND a.published_at <= ?
       ORDER BY coalesce(v.views,0) DESC, a.published_at DESC, a.news_article_id DESC LIMIT 3
       """,
            Long.class,
            from,
            to,
            now);
    return ordered(ids, now);
  }

  @Override
  public List<NewsArticle> digest(LocalDate date, OffsetDateTime now) {
    return ordered(
        jdbc.queryForList(
            "SELECT article_id FROM news_digests WHERE digest_date=? ORDER BY position",
            Long.class,
            date),
        now);
  }

  private List<NewsArticle> ordered(List<Long> ids, OffsetDateTime now) {
    if (ids.isEmpty()) return List.of();
    var a = QNewsArticle.newsArticle;
    var rows =
        queries
            .selectFrom(a)
            .where(a.id.in(ids), a.active.isTrue(), a.publishedAt.loe(now))
            .fetch();
    rows.sort(Comparator.comparingInt(x -> ids.indexOf(x.getId())));
    return rows;
  }
}
