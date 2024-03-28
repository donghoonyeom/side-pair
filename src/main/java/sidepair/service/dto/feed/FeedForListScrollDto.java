package sidepair.service.dto.feed;

import java.util.List;

public record FeedForListScrollDto(
        List<FeedForListDto> dtos,
        boolean hasNext
) {
}
