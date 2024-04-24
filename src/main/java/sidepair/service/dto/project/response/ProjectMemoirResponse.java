package sidepair.service.dto.project.response;

import sidepair.service.dto.mamber.response.MemberResponse;

public record ProjectMemoirResponse(
        MemberResponse member,
        MemoirResponse memoir
) {
}
