package sidepair.feed.configuration.response;

import java.time.LocalDateTime;

public record MemberFeedResponse(
        Long feedId,
        String feedTitle,
        LocalDateTime createdAt,
        FeedCategoryResponse category
) {

}
