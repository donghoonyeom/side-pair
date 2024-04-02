package sidepair.service.dto.feed.response;

import java.time.LocalDateTime;
import sidepair.service.dto.mamber.response.MemberResponse;

public record FeedApplicantResponse(
        Long id,
        MemberResponse member,
        LocalDateTime createdAt,
        String content
) {
}
