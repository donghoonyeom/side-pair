package sidepair.service.dto.project.response;

import sidepair.service.dto.mamber.response.MemberResponse;

public record ProjectCheckFeedResponse(
        MemberResponse member,
        CheckFeedResponse checkFeed
) {
}
