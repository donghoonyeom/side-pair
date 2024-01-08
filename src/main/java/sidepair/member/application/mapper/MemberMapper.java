package sidepair.member.application.mapper;


import java.util.LinkedHashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import sidepair.member.configuration.MemberJoinRequest;
import sidepair.member.configuration.dto.MemberJoinDto;
import sidepair.member.domain.vo.Email;
import sidepair.member.domain.vo.Nickname;
import sidepair.member.domain.vo.Password;
import sidepair.member.domain.vo.Skill;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberMapper {
    public static MemberJoinDto convertToMemberJoinDto(final MemberJoinRequest request) {
        final Email email = new Email(request.email());
        final Password password = new Password(request.password());
        final Nickname nickname = new Nickname(request.nickname());
        Set<Skill> skills = new LinkedHashSet<>();
        skills.add(request.skills());
        return new MemberJoinDto(email, password, nickname, null, skills);
    }
}
