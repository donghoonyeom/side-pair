package sidepair.service.dto.project;

import sidepair.service.dto.mamber.MemberDto;

public record ProjectMemoirDto(
        MemberDto memberDto,
        MemoirDto memoirDto
) {

}
