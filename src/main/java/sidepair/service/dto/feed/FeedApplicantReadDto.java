package sidepair.service.dto.feed;

import java.time.LocalDateTime;
import sidepair.service.dto.mamber.MemberDto;

public record FeedApplicantReadDto(
        Long id,
        MemberDto member,
        LocalDateTime createdAt,
        String content
) {
}
