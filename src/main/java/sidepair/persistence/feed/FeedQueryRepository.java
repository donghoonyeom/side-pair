package sidepair.persistence.feed;

import java.util.List;
import java.util.Optional;
import sidepair.domain.feed.Feed;
import sidepair.domain.feed.FeedCategory;
import sidepair.domain.feed.FeedStatus;
import sidepair.domain.member.Member;
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

    Optional<Feed> findByIdAndMemberEmail(final Long feedId, final String email);

    Optional<Feed> findByIdAndMember(final Long feedId, final Member member);

    List<Feed> findWithFeedContentByStatus(final FeedStatus status);
}
