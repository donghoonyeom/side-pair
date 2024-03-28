package sidepair.service.dto.feed;

import java.util.List;

public record FeedContentDto(
        Long id,
        String content,
        List<FeedNodeDto> nodes
) {
}
