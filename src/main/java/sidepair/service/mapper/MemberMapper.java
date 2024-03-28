package sidepair.service.mapper;


import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import sidepair.service.dto.mamber.MemberInformationDto;
import sidepair.service.dto.mamber.MemberInformationForPublicDto;
import sidepair.service.dto.mamber.MemberSkillDto;
import sidepair.service.dto.mamber.MemberSkillSaveDto;
import sidepair.service.dto.mamber.request.MemberJoinRequest;
import sidepair.service.dto.mamber.MemberJoinDto;
import sidepair.service.dto.mamber.request.MemberSkillSaveRequest;
import sidepair.service.dto.mamber.response.MemberInformationForPublicResponse;
import sidepair.service.dto.mamber.response.MemberInformationResponse;
import sidepair.service.dto.mamber.response.MemberSkillResponse;
import sidepair.domain.member.Position;
import sidepair.domain.member.vo.Email;
import sidepair.domain.member.vo.Nickname;
import sidepair.domain.member.vo.Password;

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
                .map(skill -> new MemberSkillResponse(skill.id(), skill.name()))
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
