package sidepair.service.dto.project;

import sidepair.service.dto.mamber.MemberDto;

public record ProjectCheckFeedDto(
        MemberDto memberDto,
        CheckFeedDto checkFeedDto
) {

}
