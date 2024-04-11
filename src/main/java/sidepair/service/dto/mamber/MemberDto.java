package sidepair.service.dto.mamber;

import java.util.List;

public record MemberDto(
        long id,
        String name,
        String imageUrl,
        String position,
        List<MemberSkillDto> skills
) {
}
