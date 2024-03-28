package sidepair.service.dto.feed.response;

import java.util.List;

public record FeedForListResponses(
        List<FeedForListResponse> responses,
        boolean hasNext
) {
}
