package sidepair.member.configuration.dto;

import sidepair.service.dto.mamber.request.PositionType;

public record OauthMemberJoinDto(
        String oauthId,
        String email,
        PositionType position,
        String nickname
) {
}