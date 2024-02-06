package sidepair.member.application.mapper;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import sidepair.member.configuration.dto.MemberInformationDto;
import sidepair.member.configuration.dto.MemberInformationForPublicDto;
import sidepair.member.configuration.request.MemberJoinRequest;
import sidepair.member.configuration.dto.MemberJoinDto;
import sidepair.member.configuration.response.MemberInformationForPublicResponse;
import sidepair.member.configuration.response.MemberInformationResponse;
import sidepair.member.domain.vo.Email;
import sidepair.member.domain.vo.Nickname;
import sidepair.member.domain.vo.Password;
import sidepair.member.domain.Skill;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberMapper {
    public static MemberJoinDto convertToMemberJoinDto(final MemberJoinRequest request) {
        final Email email = new Email(request.email());
        final Password password = new Password(request.password());
        final Nickname nickname = new Nickname(request.nickname());
        final Skill skills = Skill.valueOf(request.skills().name());
        return new MemberJoinDto(email, password, nickname, skills);
    }

    public static MemberInformationResponse convertToMemberInformationResponse(
            final MemberInformationDto memberInformationDto) {
        return new MemberInformationResponse(memberInformationDto.id(), memberInformationDto.nickname(),
                memberInformationDto.profileImageUrl(), memberInformationDto.skills(), memberInformationDto.email());
    }

    public static MemberInformationForPublicResponse convertToMemberInformationForPublicResponse(
            final MemberInformationForPublicDto memberInformationForPublicDto) {
        return new MemberInformationForPublicResponse(memberInformationForPublicDto.nickname(),
                memberInformationForPublicDto.profileImageUrl(),
                memberInformationForPublicDto.skills());
    }
}
