package sidepair.feed.configuration.dto;

import java.util.List;

public record FeedContentDto(
        Long id,
        String content,
        List<FeedNodeDto> nodes
) {
}
