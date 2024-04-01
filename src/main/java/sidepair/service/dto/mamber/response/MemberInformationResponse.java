package sidepair.service.dto.mamber.response;

import java.util.List;

public record MemberInformationResponse(
        Long id,
        String nickname,
        String profileImageUrl,
        String position,
        List<MemberSkillResponse> skills,
        String email
) {
}
