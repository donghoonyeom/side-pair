package sidepair.feed.configuration.response;

import java.util.List;

public record MemberFeedResponses(
        List<MemberFeedResponse> responses,
        boolean hasNext
) {
}
