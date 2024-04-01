package sidepair.service.dto.mamber.response;

import java.util.List;

public record MemberInformationForPublicResponse(
        String nickname,
        String profileImageUrl,
        String position,
        List<MemberSkillResponse> skills
) {

}