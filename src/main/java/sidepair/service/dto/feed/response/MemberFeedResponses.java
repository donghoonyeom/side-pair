package sidepair.service.dto.feed.response;

import java.util.List;

public record MemberFeedResponses(
        List<MemberFeedResponse> responses,
        boolean hasNext
) {
}
