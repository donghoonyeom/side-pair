package sidepair.feed.configuration.response;

import java.util.List;

public record FeedForListResponses(
        List<FeedForListResponse> responses,
        boolean hasNext
) {
}
