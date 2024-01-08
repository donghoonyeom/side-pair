package sidepair.member.configuration.dto;

import java.util.Set;
import sidepair.member.domain.vo.Email;
import sidepair.member.domain.vo.Nickname;
import sidepair.member.domain.vo.Password;
import sidepair.member.domain.vo.ProfileImgUrl;
import sidepair.member.domain.vo.Skill;

public record MemberJoinDto(

        Email email,
        Password password,
        Nickname nickname,
        ProfileImgUrl profileImgUrl,
        Set<Skill> skills

) {
}
