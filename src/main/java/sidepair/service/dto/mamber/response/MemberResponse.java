package sidepair.service.dto.mamber.response;


import java.util.List;

public record MemberResponse(
        long id,
        String name,
        String imageUrl,
        String position,
        List<MemberSkillResponse> skills
) {

}