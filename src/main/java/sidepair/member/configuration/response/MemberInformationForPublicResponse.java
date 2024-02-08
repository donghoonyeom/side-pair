package sidepair.member.configuration.response;

import java.util.List;

public record MemberInformationForPublicResponse(
        String nickname,
        String profileImageUrl,
        List<String> skills
) {

}