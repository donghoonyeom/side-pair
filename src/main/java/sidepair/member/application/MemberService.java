package sidepair.member.application;


import java.net.URL;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sidepair.global.domain.ImageContentType;
import sidepair.global.domain.NumberGenerator;
import sidepair.global.service.FileService;
import sidepair.global.service.aop.ExceptionConvert;
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
import sidepair.member.domain.vo.MemberImage;
import sidepair.member.exception.MemberException;
import sidepair.persistence.member.MemberRepository;
import sidepair.member.domain.vo.Email;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@ExceptionConvert
public class MemberService {

    private static final String DEFAULT_ORIGINAL_FILE_NAME_PROPERTY = "image.default.originalFileName";
    private static final String DEFAULT_SERVER_FILE_PATH_PROPERTY = "image.default.serverFilePath";
    private static final String DEFAULT_IMAGE_CONTENT_TYPE_PROPERTY = "image.default.imageContentType";
    private static final String DEFAULT_EXTENSION = "image.default.extension";

    private final MemberRepository memberRepository;
    private final FileService fileService;
    private final Environment environment;
    private final NumberGenerator numberGenerator;

    @Transactional
    public Long join(final MemberJoinRequest memberJoinRequest) {
        final MemberJoinDto memberJoinDto = MemberMapper.convertToMemberJoinDto(memberJoinRequest);
        checkEmailDuplicate(memberJoinDto.email());

        final EncryptedPassword encryptedPassword = new EncryptedPassword(memberJoinDto.password());
        final MemberProfile memberProfile = new MemberProfile(memberJoinDto.skills());
        final Member member = new Member(memberJoinDto.email(), encryptedPassword, memberJoinDto.nickname(),
                null, memberProfile);
        return memberRepository.save(member).getId();
    }

    private void checkEmailDuplicate(final Email email) {
        if (memberRepository.findByEmail(email).isPresent()) {
            throw new MemberException("이미 존재하는 이메일입니다.");
        }
    }

    public MemberInformationResponse findMemberInformation(final String email) {
        final Member memberWithInfo = findMemberInformationByEmail(email);
        final MemberInformationDto memberInformationDto = makeMemberInformationDto(memberWithInfo);
        return MemberMapper.convertToMemberInformationResponse(memberInformationDto);
    }

    private Member findMemberInformationByEmail(final String email) {
        return memberRepository.findWithMemberProfileAndImageByEmail(email)
                .orElseThrow(() -> new MemberException("조회한 멤버가 존재하지 않습니다."));
    }

    public MemberInformationDto makeMemberInformationDto(final Member member) {
        final MemberImage memberImage = member.getImage();
        final MemberProfile memberProfile = member.getMemberProfile();
        final URL imageUrl = fileService.generateUrl(memberImage.getServerFilePath(), HttpMethod.GET);
        return new MemberInformationDto(member.getId(), member.getNickname().getValue(),
                imageUrl.toExternalForm(), Collections.singletonList(memberProfile.getSkills().name()),
                member.getEmail().getValue());
    }
    private MemberImage findDefaultMemberImage() {
        final String defaultOriginalFileName = environment.getProperty(DEFAULT_ORIGINAL_FILE_NAME_PROPERTY);
        final String defaultServerFilePath = environment.getProperty(DEFAULT_SERVER_FILE_PATH_PROPERTY);
        final String defaultImageContentType = environment.getProperty(DEFAULT_IMAGE_CONTENT_TYPE_PROPERTY);
        final String defaultExtension = environment.getProperty(DEFAULT_EXTENSION);
        final int randomImageNumber = numberGenerator.generate();
        return new MemberImage(defaultOriginalFileName + randomImageNumber,
                defaultServerFilePath + randomImageNumber + defaultExtension,
                ImageContentType.valueOf(defaultImageContentType));
    }

    public MemberInformationForPublicResponse findMemberInformationForPublic(final Long memberId) {
        final Member memberWithPublicInfo = findMemberInformationByMemberId(memberId);
        final URL memberimageURl = fileService.generateUrl(memberWithPublicInfo.getImage().getServerFilePath(),
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
