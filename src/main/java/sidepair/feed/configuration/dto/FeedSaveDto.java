package sidepair.feed.configuration.dto;

import java.util.List;

public record FeedSaveDto(

        Long categoryId,
        String title,
        String introduction,
        String content,
        Integer requiredPeriod,
        List<FeedNodeSaveDto> feedNodes,
        List<FeedTagSaveDto> tags
) {
}
