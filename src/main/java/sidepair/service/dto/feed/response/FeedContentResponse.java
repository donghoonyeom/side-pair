package sidepair.service.dto.feed.response;

import java.util.List;

public record FeedContentResponse(
        Long id,
        String content,
        List<FeedNodeResponse> nodes
) {

}

