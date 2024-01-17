package sidepair.member.configuration.dto;

import java.util.List;

public record MemberInformationDto (
        Long id,
        String nickname,
        String profileImageUrl,
        List<String> skills,
        String email
) {

}
