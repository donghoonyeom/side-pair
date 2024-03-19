package sidepair.member.configuration.dto;

public record OauthMemberJoinDto(
        String oauthId,
        String email,
        String nickname
) {
}