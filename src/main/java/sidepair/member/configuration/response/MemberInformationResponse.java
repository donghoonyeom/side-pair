package sidepair.member.configuration.response;

import java.util.List;

public record MemberInformationResponse(
        Long id,
        String nickname,
        String profileImageUrl,
        List<String> skills,
        String email
) {
}
