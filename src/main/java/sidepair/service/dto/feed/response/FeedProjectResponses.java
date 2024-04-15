package sidepair.service.dto.feed.response;

import java.util.List;

public record FeedProjectResponses(
        List<FeedProjectResponse> responses,
        boolean hasNext
) {

}
