package sidepair.member.application;


import java.net.URL;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sidepair.global.service.FileService;
import sidepair.global.service.exception.NotFoundException;
import sidepair.member.application.mapper.MemberMapper;
import sidepair.member.configuration.dto.MemberInformationDto;
import sidepair.member.configuration.dto.MemberInformationForPublicDto;
import sidepair.member.configuration.request.MemberJoinRequest;
import sidepair.member.configuration.dto.MemberJoinDto;
import sidepair.member.configuration.response.MemberInformationForPublicResponse;
import sidepair.member.configuration.response.MemberInformationResponse;
import sidepair.member.domain.EncryptedPassword;
import sidepair.member.domain.Member;
import sidepair.member.domain.MemberProfile;
import sidepair.member.domain.MemberRepository;
import sidepair.member.domain.vo.Email;
import sidepair.member.domain.vo.ProfileImgUrl;
import sidepair.member.exception.MemberException.DuplicateEmailException;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final FileService fileService;

    @Transactional
    public Long join(final MemberJoinRequest memberJoinRequest) {
        final MemberJoinDto memberJoinDto = MemberMapper.convertToMemberJoinDto(memberJoinRequest);
        checkEmailDuplicate(memberJoinDto.email());

        final EncryptedPassword encryptedPassword = new EncryptedPassword(memberJoinDto.password());
        final ProfileImgUrl profileImgUrl = null;
        final Member member = new Member(memberJoinDto.nickname(), memberJoinDto.email(), encryptedPassword,
                memberJoinDto.skills(), memberJoinDto.profileImgUrl());
        return memberRepository.save(member).getId();
    }

    private void checkEmailDuplicate(final Email email) {
        if (memberRepository.findByEmail(email).isPresent()) {
            throw new DuplicateEmailException(email);
        }
    }

    public MemberInformationResponse findMemberInformation(final String email) {
        final Member memberWithInfo = findMemberInformationByIdentifier(email);
        final MemberInformationDto memberInformationDto = makeMemberInformationDto(memberWithInfo);
        return MemberMapper.convertToMemberInformationResponse(memberInformationDto);
    }

    private Member findMemberInformationByIdentifier(final String email) {
        return memberRepository.findWithMemberProfileAndImageByEmail(email)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다."));
    }

    public MemberInformationDto makeMemberInformationDto(final Member member) {
        final ProfileImgUrl memberImage = member.getProfileImgUrl();
        final MemberProfile memberProfile = member.getMemberProfile();
        final URL imageUrl = fileService.generateUrl(memberImage.getValue(), HttpMethod.GET);
        return new MemberInformationDto(member.getId(), member.getNickname().getValue(),
                imageUrl.toExternalForm(), Collections.singletonList(memberProfile.getSkills().name()),
                memberProfile.getEmail());
    }

    public MemberInformationForPublicResponse findMemberInformationForPublic(final Long memberId) {
        final Member memberWithPublicInfo = findMemberInformationByMemberId(memberId);
        final URL memberimageURl = fileService.generateUrl(memberWithPublicInfo.getProfileImgUrl().getValue(),
                HttpMethod.GET);
        final MemberInformationForPublicDto memberInformationForPublicDto =
                new MemberInformationForPublicDto(memberWithPublicInfo.getNickname().getValue(),
                        memberimageURl.toExternalForm(),
                        Collections.singletonList(memberWithPublicInfo.getMemberProfile().getSkills().name()));
        return MemberMapper.convertToMemberInformationForPublicResponse(memberInformationForPublicDto);
    }

    private Member findMemberInformationByMemberId(final Long memberId) {
        return memberRepository.findWithMemberProfileAndImageById(memberId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다. memberId = " + memberId));
    }
}
