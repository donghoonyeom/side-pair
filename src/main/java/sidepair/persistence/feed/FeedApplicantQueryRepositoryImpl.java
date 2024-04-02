package sidepair.persistence.feed;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import java.time.LocalDateTime;
import java.util.List;
import sidepair.domain.feed.Feed;
import sidepair.domain.feed.FeedApplicant;
import sidepair.persistence.QuerydslRepositorySupporter;

import static sidepair.domain.feed.QFeedApplicant.feedApplicant;
import static sidepair.domain.member.QMember.member;

public class FeedApplicantQueryRepositoryImpl extends QuerydslRepositorySupporter
        implements FeedApplicantQueryRepository {

    public FeedApplicantQueryRepositoryImpl(){
        super(FeedApplicant.class);
    }

    @Override
    public List<FeedApplicant> findFeedApplicantWithMemberByFeedOrderByLatest(final Feed feed,
                                                                              final Long lastId,
                                                                              final int pageSize) {
        return selectFrom(feedApplicant)
                .innerJoin(feedApplicant.member, member)
                .fetchJoin()
                .where(feedCond(feed), lessThanLastId(lastId))
                .limit(pageSize)
                .orderBy(orderByCreatedAtDesc())
                .fetch();
    }

    private BooleanExpression feedCond(final Feed feed) {
        return feedApplicant.feed.eq(feed);
    }

    private BooleanExpression lessThanLastId(final Long lastId) {
        if (lastId == null) {
            return null;
        }
        return feedApplicant.createdAt.lt(
                select(feedApplicant.createdAt).from(feedApplicant).where(feedApplicant.id.eq(lastId))
        );
    }

    private OrderSpecifier<LocalDateTime> orderByCreatedAtDesc() {
        return feedApplicant.createdAt.desc();
    }
}
