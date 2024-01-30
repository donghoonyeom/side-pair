package sidepair.member.configuration.dto;

import sidepair.member.domain.Skill;
import sidepair.member.domain.vo.Email;
import sidepair.member.domain.vo.Nickname;
import sidepair.member.domain.vo.Password;

public record MemberJoinDto(

        Email email,
        Password password,
        Nickname nickname,
        Skill skills

) {
}
