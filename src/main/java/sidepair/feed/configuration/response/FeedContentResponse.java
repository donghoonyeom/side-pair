package sidepair.feed.configuration.response;

import java.util.List;

public record FeedContentResponse(
        Long id,
        String content,
        List<FeedNodeResponse> nodes
) {

}

