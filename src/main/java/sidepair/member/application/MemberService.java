package sidepair.member.application;


import java.net.URL;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sidepair.global.domain.ImageContentType;
import sidepair.global.domain.NumberGenerator;
import sidepair.member.configuration.dto.MemberSkillDto;
import sidepair.member.configuration.dto.MemberSkillSaveDto;
import sidepair.member.configuration.dto.OauthMemberJoinDto;
import sidepair.member.domain.MemberSkill;
import sidepair.member.domain.MemberSkills;
import sidepair.member.domain.Position;
import sidepair.member.domain.vo.Nickname;
import sidepair.member.domain.vo.SkillName;
import sidepair.persistence.auth.RefreshTokenRepository;
import sidepair.service.FileService;
import sidepair.service.aop.ExceptionConvert;
import sidepair.service.auth.TokenProvider;
import sidepair.service.dto.auth.response.AuthenticationResponse;
import sidepair.service.exception.ConflictException;
import sidepair.service.exception.NotFoundException;
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
import sidepair.persistence.member.MemberRepository;
import sidepair.member.domain.vo.Email;
import sidepair.service.mapper.AuthMapper;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@ExceptionConvert
public class MemberService {

    private static final String DEFAULT_ORIGINAL_FILE_NAME_PROPERTY = "image.default.originalFileName";
    private static final String DEFAULT_SERVER_FILE_PATH_PROPERTY = "image.default.serverFilePath";
    private static final String DEFAULT_IMAGE_CONTENT_TYPE_PROPERTY = "image.default.imageContentType";
    private static final String DEFAULT_EXTENSION = "image.default.extension";
    private static final String DEFAULT_SKILLS_VALUE = "etc";

    private final MemberRepository memberRepository;
    private final FileService fileService;
    private final Environment environment;
    private final TokenProvider tokenProvider;
    private final NumberGenerator numberGenerator;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public Long join(final MemberJoinRequest memberJoinRequest) {
        final MemberJoinDto memberJoinDto = MemberMapper.convertToMemberJoinDto(memberJoinRequest);
        checkEmailDuplicate(memberJoinDto.email());

        final EncryptedPassword encryptedPassword = new EncryptedPassword(memberJoinDto.password());
        final MemberSkills memberSkills = saveMemberskills(memberJoinDto.skills());
        final MemberProfile memberProfile = new MemberProfile(memberJoinDto.position());
        final Member member = new Member(memberJoinDto.email(), encryptedPassword, memberJoinDto.nickname(),
                findDefaultMemberImage(), memberProfile, memberSkills);

        return memberRepository.save(member).getId();
    }

    private void checkEmailDuplicate(final Email email) {
        if (memberRepository.findByEmail(email).isPresent()) {
            throw new ConflictException("이미 존재하는 이메일입니다.");
        }
    }

    private MemberSkills saveMemberskills(final List<MemberSkillSaveDto> skillSaveDto) {
        return new MemberSkills(
                skillSaveDto.stream()
                        .map(skill -> new MemberSkill(new SkillName(skill.name())))
                        .toList()
        );
    }

    private MemberSkills saveDefaultMemberSkills() {
        return new MemberSkills(List.of(new MemberSkill(1L, new SkillName(DEFAULT_SKILLS_VALUE))));
    }

    @Transactional
    public AuthenticationResponse oauthJoin(final OauthMemberJoinDto oauthMemberJoinDto) {
        final MemberProfile memberProfile = new MemberProfile(Position.valueOf(oauthMemberJoinDto.position().name()));
        final Nickname nickname = new Nickname(oauthMemberJoinDto.nickname());
        final Email email = new Email(oauthMemberJoinDto.email());
        final MemberSkills memberSkills = saveDefaultMemberSkills();
        final Member member = new Member(email, oauthMemberJoinDto.oauthId(), nickname, findDefaultMemberImage(),
                memberProfile, memberSkills);
        memberRepository.save(member);
        return makeAuthenticationResponse(member);
    }

    private AuthenticationResponse makeAuthenticationResponse(final Member member) {
        final String refreshToken = tokenProvider.createRefreshToken(member.getEmail().getValue(), Map.of());
        saveRefreshToken(refreshToken, member);
        final String accessToken = tokenProvider.createAccessToken(member.getEmail().getValue(), Map.of());
        return AuthMapper.convertToAuthenticationResponse(refreshToken, accessToken);
    }

    private void saveRefreshToken(final String refreshToken, final Member member) {
        refreshTokenRepository.save(refreshToken, member.getEmail().getValue());
    }

    public MemberInformationResponse findMemberInformation(final String email) {
        final Member memberWithInfo = findMemberInformationByEmail(email);
        final MemberInformationDto memberInformationDto = makeMemberInformationDto(memberWithInfo);
        return MemberMapper.convertToMemberInformationResponse(memberInformationDto);
    }

    private Member findMemberInformationByEmail(final String email) {
        return memberRepository.findWithMemberProfileAndImageByEmail(email)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다."));
    }

    public MemberInformationDto makeMemberInformationDto(final Member member) {
        final MemberImage memberImage = member.getImage();
        final MemberProfile memberProfile = member.getMemberProfile();
        final URL imageUrl = fileService.generateUrl(memberImage.getServerFilePath(), HttpMethod.GET);
        return new MemberInformationDto(member.getId(), member.getNickname().getValue(),
                imageUrl.toExternalForm(), memberProfile.getPosition().name(),
                makeMemberSkillDtos(member.getSkills()), member.getEmail().getValue());
    }

    private List<MemberSkillDto> makeMemberSkillDtos(final MemberSkills memberSkills) {
        return memberSkills.getValues()
                .stream()
                .map(it -> new MemberSkillDto(it.getId(), it.getName().getValue()))
                .toList();
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
                        memberimageURl.toExternalForm(), memberWithPublicInfo.getMemberProfile().getPosition().name(),
                        makeMemberSkillDtos(memberWithPublicInfo.getSkills()));
        return MemberMapper.convertToMemberInformationForPublicResponse(memberInformationForPublicDto);
    }

    private Member findMemberInformationByMemberId(final Long memberId) {
        return memberRepository.findWithMemberProfileAndImageById(memberId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다. memberId = " + memberId));
    }
}
