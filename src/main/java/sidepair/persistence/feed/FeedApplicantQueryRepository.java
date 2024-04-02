package sidepair.persistence.feed;

import java.util.List;
import sidepair.domain.feed.Feed;
import sidepair.domain.feed.FeedApplicant;

public interface FeedApplicantQueryRepository {
    List<FeedApplicant> findFeedApplicantWithMemberByFeedOrderByLatest(final Feed feed,
                                                                       final Long lastId,
                                                                       final int pageSize);
}
