package sidepair.service.dto.feed;

import sidepair.domain.member.Member;

public record FeedApplicantDto(
        String content,
        Member member
) {
}
