package sidepair.member.configuration.dto;

import java.util.List;

public record MemberInformationForPublicDto(
        String nickname,
        String profileImageUrl,
        String position,
        List<MemberSkillDto> skills
) {

}
