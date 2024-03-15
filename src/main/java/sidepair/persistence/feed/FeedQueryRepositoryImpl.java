package sidepair.persistence.feed;

import static sidepair.feed.domain.QFeed.feed;
import static sidepair.feed.domain.QFeedCategory.feedCategory;
import static sidepair.feed.domain.QFeedContent.feedContent;
import static sidepair.feed.domain.QFeedTag.feedTag;
import static sidepair.member.domain.QMember.member;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQuery;
import java.util.List;
import java.util.Optional;
import sidepair.feed.domain.Feed;
import sidepair.feed.domain.FeedCategory;
import sidepair.feed.domain.FeedStatus;
import sidepair.member.domain.Member;
import sidepair.member.domain.vo.Email;
import sidepair.persistence.QuerydslRepositorySupporter;
import sidepair.persistence.dto.FeedOrderType;
import sidepair.persistence.dto.FeedSearchCreatorNickname;
import sidepair.persistence.dto.FeedSearchDto;
import sidepair.persistence.dto.FeedSearchTagName;
import sidepair.persistence.dto.FeedSearchTitle;

public class FeedQueryRepositoryImpl extends QuerydslRepositorySupporter implements FeedQueryRepository {

    private static final int LIMIT_OFFSET = 1;

    public FeedQueryRepositoryImpl() {
        super(Feed.class);
    }

    @Override
    public Optional<Feed> findFeedById(final Long feedId) {
        return Optional.ofNullable(selectFrom(feed)
                .innerJoin(feed.creator, member)
                .fetchJoin()
                .innerJoin(feed.category, feedCategory)
                .fetchJoin()
                .leftJoin(feed.tags.values, feedTag)
                .where(feedCond(feedId))
                .fetchOne());
    }

    @Override
    public List<Feed> findFeedsByCategory(final FeedCategory category, final FeedOrderType orderType,
                                          final Long lastId, final int pageSize) {

        return selectFrom(feed)
                .innerJoin(feed.category, feedCategory)
                .fetchJoin()
                .innerJoin(feed.creator, member)
                .fetchJoin()
                .where(
                        lessThanLastId(lastId, orderType),
                        statusCond(FeedStatus.RECRUITING),
                        categoryCond(category))
                .limit(pageSize + LIMIT_OFFSET)
                .orderBy(sortCond(orderType))
                .fetch();
    }

    @Override
    public List<Feed> findFeedsByCond(final FeedSearchDto searchRequest, final FeedOrderType orderType,
                                      final Long lastId, final int pageSize) {
        return selectFrom(feed)
                .innerJoin(feed.category, feedCategory)
                .fetchJoin()
                .innerJoin(feed.creator, member)
                .fetchJoin()
                .where(
                        lessThanLastId(lastId, orderType),
                        statusCond(FeedStatus.RECRUITING),
                        titleCond(searchRequest.getTitle()),
                        creatorNicknameCond(searchRequest.getCreatorName()),
                        tagCond(searchRequest.getTagName()))
                .limit(pageSize + LIMIT_OFFSET)
                .orderBy(sortCond(orderType))
                .fetch();
    }

    @Override
    public List<Feed> findFeedsWithCategoryByMemberOrderByLatest(final Member member,
                                                                       final Long lastId,
                                                                       final int pageSize) {
        final FeedOrderType orderType = FeedOrderType.LATEST;
        return selectFrom(feed)
                .innerJoin(feed.category, feedCategory)
                .fetchJoin()
                .where(
                        creatorIdCond(member.getId()),
                        lessThanLastId(lastId, orderType))
                .limit(pageSize + LIMIT_OFFSET)
                .orderBy(sortCond(orderType))
                .fetch();
    }

    @Override
    public Optional<Feed> findByIdAndMemberEmail(final Long feedId, final String email) {
        return Optional.ofNullable(selectFrom(feed)
                .where(creatorEmailCond(email),
                        feedCond(feedId))
                .fetchOne());
    }

    @Override
    public List<Feed> findWithFeedContentByStatus(final FeedStatus status) {
        return selectFrom(feed)
                .innerJoin(feed.contents.values, feedContent)
                .fetchJoin()
                .where(statusCond(status))
                .fetch();
    }

    private BooleanExpression feedCond(final Long feedId) {
        return feed.id.eq(feedId);
    }

    private BooleanExpression categoryCond(final FeedCategory category) {
        if (category == null) {
            return null;
        }
        return feed.category.eq(category);
    }

    private BooleanExpression statusCond(final FeedStatus status) {
        return feed.status.eq(status);
    }

    private BooleanExpression titleCond(final FeedSearchTitle title) {
        if (title == null) {
            return null;
        }
        return removeBlank(feed.title).containsIgnoreCase(title.value());
    }

    private StringExpression removeBlank(final StringExpression field) {
        return Expressions.stringTemplate("REPLACE({0}, ' ', '')", field);
    }

    private BooleanExpression creatorIdCond(final Long creatorId) {
        if (creatorId == null) {
            return null;
        }
        return feed.creator.id.eq(creatorId);
    }

    private BooleanExpression creatorNicknameCond(final FeedSearchCreatorNickname creatorName) {
        if (creatorName == null) {
            return null;
        }
        return feed.creator.nickname.value.eq(creatorName.value());
    }

    private BooleanExpression tagCond(final FeedSearchTagName tagName) {
        if (tagName == null) {
            return null;
        }
        return feed.tags.values
                .any()
                .name.value
                .equalsIgnoreCase(tagName.value());
    }

    private OrderSpecifier<?> sortCond(final FeedOrderType orderType) {
        return feed.createdAt.desc();
    }

    private BooleanExpression lessThanLastId(final Long lastId, final FeedOrderType orderType) {
        if (lastId == null) {
            return null;
        }
        return feed.createdAt.lt(
                select(feed.createdAt)
                        .from(feed)
                        .where(feed.id.eq(lastId))
        );
    }

    private BooleanExpression creatorEmailCond(final String email) {
        final Email creatorEmail = new Email(email);
        return feed.creator.email.eq(creatorEmail);
    }
}

