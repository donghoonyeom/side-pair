package sidepair.feed.configuration.dto;

import java.util.List;

public record FeedForListScrollDto(
        List<FeedForListDto> dtos,
        boolean hasNext
) {
}
