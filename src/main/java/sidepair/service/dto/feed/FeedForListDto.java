package sidepair.service.dto.feed;

import java.time.LocalDateTime;
import java.util.List;
import sidepair.service.dto.mamber.MemberDto;

public record FeedForListDto(
        long feedId,
        String feedTitle,
        String introduction,
        int recommendedFeedPeriod,
        LocalDateTime createdAt,
        MemberDto creator,
        FeedCategoryDto category,
        List<FeedTagDto> tags
) {

}
