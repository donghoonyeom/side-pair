package sidepair.service.dto.mamber;

import java.util.List;
import sidepair.domain.member.Position;
import sidepair.domain.member.vo.Email;
import sidepair.domain.member.vo.Nickname;
import sidepair.domain.member.vo.Password;

public record MemberJoinDto(

        Email email,
        Password password,
        Nickname nickname,
        Position position,
        List<MemberSkillSaveDto> skills

) {
}
