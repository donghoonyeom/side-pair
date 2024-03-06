package sidepair.persistence.feed;

import java.util.List;
import java.util.Optional;
import sidepair.feed.domain.Feed;
import sidepair.feed.domain.FeedCategory;
import sidepair.feed.domain.FeedStatus;
import sidepair.member.domain.Member;
import sidepair.persistence.dto.FeedOrderType;
import sidepair.persistence.dto.FeedSearchDto;

public interface FeedQueryRepository {
    Optional<Feed> findFeedById(final Long feedId);

    List<Feed> findFeedsByCategory(final FeedCategory category,
                                         final FeedOrderType orderType,
                                         final Long lastId,
                                         final int pageSize);

    List<Feed> findFeedsByCond(final FeedSearchDto searchRequest,
                                     final FeedOrderType orderType,
                                     final Long lastId,
                                     final int pageSize);

    List<Feed> findFeedsWithCategoryByMemberOrderByLatest(final Member member,
                                                                final Long lastId,
                                                                final int pageSize);

    Optional<Feed> findByIdAndMemberEmail(final Long roadmapId, final String email);

    List<Feed> findWithFeedContentByStatus(final FeedStatus status);
}
