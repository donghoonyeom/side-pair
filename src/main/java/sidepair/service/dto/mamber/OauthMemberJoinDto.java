package sidepair.service.dto.mamber;

import sidepair.service.dto.mamber.request.PositionType;

public record OauthMemberJoinDto(
        String oauthId,
        String email,
        PositionType position,
        String nickname
) {
}