package sidepair.service.dto.project.response;

import java.util.List;
import sidepair.service.dto.mamber.response.MemberSkillResponse;

public record ProjectMemberResponse(
        Long memberId,
        String nickname,
        String imagePath,
        Double participationRate,
        String position,
        List<MemberSkillResponse> skills
) {

}
