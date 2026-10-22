package seed.seedplusbackend.community.infrastructure.query;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Repository;
import seed.seedplusbackend.community.application.port.CommunityPostSearchPort;
import seed.seedplusbackend.community.application.query.CommunityPostSearchQuery;
import seed.seedplusbackend.community.domain.entity.CommunityPost;
import seed.seedplusbackend.community.domain.entity.QCommunityPost;
import seed.seedplusbackend.industry.domain.entity.QIndustry;
import seed.seedplusbackend.region.domain.entity.QRegion;

@Repository
@RequiredArgsConstructor
public class QuerydslCommunityPostSearchAdapter implements CommunityPostSearchPort {
  private final JPAQueryFactory queryFactory;

  @Override
  public Page<CommunityPost> search(CommunityPostSearchQuery query) {
    QCommunityPost post = QCommunityPost.communityPost;
    QCommunityPost taggedPost = new QCommunityPost("taggedPost");
    QRegion region = new QRegion("tagRegion");
    QIndustry industry = new QIndustry("tagIndustry");
    QIndustry parent = new QIndustry("tagParent");
    QIndustry grandparent = new QIndustry("tagGrandparent");
    BooleanBuilder conditions = new BooleanBuilder(post.deletedAt.isNull());
    if (!query.regionIds().isEmpty()) {
      conditions.and(
          JPAExpressions.selectOne()
              .from(taggedPost)
              .join(taggedPost.regions, region)
              .where(taggedPost.id.eq(post.id), region.id.in(query.regionIds()))
              .exists());
    }
    if (!query.industryIds().isEmpty()) {
      conditions.and(
          JPAExpressions.selectOne()
              .from(taggedPost)
              .join(taggedPost.industries, industry)
              .leftJoin(industry.parentIndustry, parent)
              .leftJoin(parent.parentIndustry, grandparent)
              .where(
                  taggedPost.id.eq(post.id),
                  industry
                      .id
                      .in(query.industryIds())
                      .or(parent.id.in(query.industryIds()))
                      .or(grandparent.id.in(query.industryIds())))
              .exists());
    }
    if (query.keyword() != null && !query.keyword().isBlank()) {
      String keyword = query.keyword().trim();
      conditions.and(
          post.title
              .containsIgnoreCase(keyword)
              .or(post.content.containsIgnoreCase(keyword))
              .or(
                  JPAExpressions.selectOne()
                      .from(taggedPost)
                      .join(taggedPost.regions, region)
                      .where(
                          taggedPost.id.eq(post.id),
                          region
                              .sigungu
                              .containsIgnoreCase(keyword)
                              .or(region.sido.containsIgnoreCase(keyword)))
                      .exists())
              .or(
                  JPAExpressions.selectOne()
                      .from(taggedPost)
                      .join(taggedPost.industries, industry)
                      .where(taggedPost.id.eq(post.id), industry.name.containsIgnoreCase(keyword))
                      .exists()));
    }
    OrderSpecifier<?> order =
        query.pageable().getSort().getOrderFor("likeCount") == null
            ? post.createdAt.desc()
            : post.likeCount.desc();
    List<CommunityPost> content =
        queryFactory
            .selectFrom(post)
            .join(post.user)
            .fetchJoin()
            .where(conditions)
            .orderBy(order, post.id.desc())
            .offset(query.pageable().getOffset())
            .limit(query.pageable().getPageSize())
            .fetch();
    Long total = queryFactory.select(post.count()).from(post).where(conditions).fetchOne();
    return new PageImpl<>(content, query.pageable(), total == null ? 0 : total);
  }
}
