package sidepair.service.dto.mamber;

import java.util.List;

public record MemberInformationDto (
        Long id,
        String nickname,
        String profileImageUrl,
        String position,
        List<MemberSkillDto> skills,
        String email
) {

}
