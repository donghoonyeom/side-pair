package sidepair.service.dto.feed.response;

import java.time.LocalDateTime;
import java.util.List;
import sidepair.feed.configuration.response.FeedCategoryResponse;
import sidepair.feed.configuration.response.FeedContentResponse;
import sidepair.feed.configuration.response.FeedTagResponse;
import sidepair.global.service.dto.mamber.response.MemberResponse;

public record FeedResponse(
        Long feedId,
        FeedCategoryResponse category,
        String feedTitle,
        String introduction,
        MemberResponse creator,
        FeedContentResponse content,
        int recommendedFeedPeriod,
        LocalDateTime createdAt,
        List<FeedTagResponse> tags

        // todo: 프로젝트 추가
) {
}
