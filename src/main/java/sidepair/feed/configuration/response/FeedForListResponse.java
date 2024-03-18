package sidepair.feed.configuration.response;

import java.time.LocalDateTime;
import java.util.List;
import sidepair.service.dto.mamber.response.MemberResponse;

public record FeedForListResponse(
        long feedId,
        String feedTitle,
        String introduction,
        int recommendedFeedPeriod,
        LocalDateTime createdAt,
        MemberResponse creator,
        FeedCategoryResponse category,
        List<FeedTagResponse> tags
) {
}
