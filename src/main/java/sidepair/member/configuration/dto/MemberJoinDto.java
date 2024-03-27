package sidepair.member.configuration.dto;

import java.util.List;
import sidepair.member.configuration.request.MemberSkillSaveRequest;
import sidepair.member.domain.Position;
import sidepair.member.domain.vo.Email;
import sidepair.member.domain.vo.Nickname;
import sidepair.member.domain.vo.Password;

public record MemberJoinDto(

        Email email,
        Password password,
        Nickname nickname,
        Position position,
        List<MemberSkillSaveDto> skills

) {
}
