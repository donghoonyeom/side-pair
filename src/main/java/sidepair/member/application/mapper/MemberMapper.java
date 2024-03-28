package sidepair.member.application.mapper;


import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import sidepair.member.configuration.dto.MemberInformationDto;
import sidepair.member.configuration.dto.MemberInformationForPublicDto;
import sidepair.member.configuration.dto.MemberSkillDto;
import sidepair.member.configuration.dto.MemberSkillSaveDto;
import sidepair.member.configuration.request.MemberJoinRequest;
import sidepair.member.configuration.dto.MemberJoinDto;
import sidepair.member.configuration.request.MemberSkillSaveRequest;
import sidepair.member.configuration.response.MemberInformationForPublicResponse;
import sidepair.member.configuration.response.MemberInformationResponse;
import sidepair.member.configuration.response.MemberSkillResponse;
import sidepair.member.domain.Position;
import sidepair.member.domain.vo.Email;
import sidepair.member.domain.vo.Nickname;
import sidepair.member.domain.vo.Password;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberMapper {
    public static MemberJoinDto convertToMemberJoinDto(final MemberJoinRequest request) {
        final Email email = new Email(request.email());
        final Password password = new Password(request.password());
        final Nickname nickname = new Nickname(request.nickname());
        final Position position = Position.valueOf(request.positionType().name());
        final List<MemberSkillSaveDto> skills = convertToMemberSkillSaveDtos(request);
        return new MemberJoinDto(email, password, nickname, position, skills);
    }

    public static MemberInformationResponse convertToMemberInformationResponse(
            final MemberInformationDto memberInformationDto) {
        return new MemberInformationResponse(memberInformationDto.id(), memberInformationDto.nickname(),
                memberInformationDto.profileImageUrl(), memberInformationDto.position(),
                convertMemberSkillResponses(memberInformationDto.skills()), memberInformationDto.email());
    }

    public static MemberInformationForPublicResponse convertToMemberInformationForPublicResponse(
            final MemberInformationForPublicDto memberInformationForPublicDto) {
        return new MemberInformationForPublicResponse(memberInformationForPublicDto.nickname(),
                memberInformationForPublicDto.profileImageUrl(), memberInformationForPublicDto.position(),
                convertMemberSkillResponses(memberInformationForPublicDto.skills()));
    }

    private static List<MemberSkillResponse> convertMemberSkillResponses(final List<MemberSkillDto> memberSkillDtos) {
        return memberSkillDtos.stream()
                .map(tag -> new MemberSkillResponse(tag.id(), tag.name()))
                .toList();
    }

    private static MemberSkillSaveDto convertToMemberSkillSaveDto(final MemberSkillSaveRequest request) {
        return new MemberSkillSaveDto(request.name());
    }

    private static List<MemberSkillSaveDto> convertToMemberSkillSaveDtos(final MemberJoinRequest request) {
        if (request.skills() == null) {
            return Collections.emptyList();
        }
        return request.skills()
                .stream()
                .map(MemberMapper::convertToMemberSkillSaveDto)
                .toList();
    }
}
