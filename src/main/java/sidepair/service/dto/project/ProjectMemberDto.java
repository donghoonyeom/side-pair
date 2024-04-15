package sidepair.service.dto.project;

import java.util.List;
import sidepair.service.dto.mamber.MemberSkillDto;

public record ProjectMemberDto(
        Long memberId,
        String nickname,
        String imagePath,
        Double participationRate,
        String position,
        List<MemberSkillDto> skills
) {

}
