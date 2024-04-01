package sidepair.service.dto.feed;

import java.time.LocalDateTime;
import java.util.List;
import sidepair.service.dto.mamber.MemberDto;

public record FeedDto(
        Long feedId,
        FeedCategoryDto category,
        String feedTitle,
        String introduction,
        MemberDto creator,
        FeedContentDto content,
        int recommendedFeedPeriod,
        LocalDateTime createdAt,
        List<FeedTagDto> tags
) {

}
