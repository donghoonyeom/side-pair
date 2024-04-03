package sidepair.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import sidepair.domain.ImageContentType;
import sidepair.domain.member.EncryptedPassword;
import sidepair.domain.member.Member;
import sidepair.domain.member.MemberProfile;
import sidepair.domain.member.MemberSkill;
import sidepair.domain.member.MemberSkills;
import sidepair.domain.member.Position;
import sidepair.domain.member.vo.Email;
import sidepair.domain.member.vo.MemberImage;
import sidepair.domain.member.vo.Nickname;
import sidepair.domain.member.vo.Password;
import sidepair.domain.member.vo.SkillName;
import sidepair.persistence.auth.RefreshTokenRepository;
import sidepair.persistence.member.MemberRepository;
import sidepair.service.FileService;
import sidepair.service.NumberGenerator;
import sidepair.service.auth.TokenProvider;
import sidepair.service.dto.auth.response.AuthenticationResponse;
import sidepair.service.dto.mamber.OauthMemberJoinDto;
import sidepair.service.dto.mamber.request.MemberJoinRequest;
import sidepair.service.dto.mamber.request.MemberSkillSaveRequest;
import sidepair.service.dto.mamber.request.PositionType;
import sidepair.service.dto.mamber.response.MemberInformationForPublicResponse;
import sidepair.service.dto.mamber.response.MemberInformationResponse;
import sidepair.service.dto.mamber.response.MemberSkillResponse;
import sidepair.service.exception.ConflictException;
import sidepair.service.exception.NotFoundException;
import sidepair.service.member.MemberService;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    private static final String IMAGE_DEFAULT_ORIGINAL_FILE_NAME_PROPERTY = "image.default.originalFileName";
    private static final String IMAGE_DEFAULT_SERVER_FILE_PATH_PROPERTY = "image.default.serverFilePath";
    private static final String IMAGE_DEFAULT_IMAGE_CONTENT_TYPE_PROPERTY = "image.default.imageContentType";

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private Environment environment;

    @Mock
    private NumberGenerator numberGenerator;

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private FileService fileService;

    @InjectMocks
    private MemberService memberService;

    @Test
    void 회원가입을_한다() {
        //given
        final MemberJoinRequest request = new MemberJoinRequest("sidepair@email.com", "password1!",
                "nickname", PositionType.BACKEND, List.of(new MemberSkillSaveRequest("Java")));

        given(memberRepository.findByEmail(any()))
                .willReturn(Optional.empty());
        given(memberRepository.save(any()))
                .willReturn(new Member(1L, null, null, null, null, null, null, null));
        given(environment.getProperty(IMAGE_DEFAULT_ORIGINAL_FILE_NAME_PROPERTY))
                .willReturn("default-member-image");
        given(environment.getProperty(IMAGE_DEFAULT_SERVER_FILE_PATH_PROPERTY))
                .willReturn("https://blog.kakaocdn.net/dn/nGKCL/btsF9BfyYTT/R0ERaU79wCKH2eHylGxK1k/img.png");
        given(environment.getProperty(IMAGE_DEFAULT_IMAGE_CONTENT_TYPE_PROPERTY))
                .willReturn("JPG");
        given(numberGenerator.generate())
                .willReturn(7);

        //when
        //then
        assertThat(memberService.join(request))
                .isEqualTo(1L);
    }

    @Test
    void 회원가입_시_이미_존재하는_아이디가_존재할때_예외를_던진다() {
        //given
        final MemberJoinRequest request = new MemberJoinRequest("sidepair@email.com", "password1!",
                "nickname",PositionType.BACKEND, List.of(new MemberSkillSaveRequest("Java")));
        final Email email = new Email("sidepair@email.com");
        final Password password = new Password("password1!");
        final MemberSkills skills = new MemberSkills(List.of(new MemberSkill(1L, new SkillName("Java"))));
        final Nickname nickname = new Nickname("nickname");

        final Member member = new Member(email, new EncryptedPassword(password), nickname, null,
                new MemberProfile(Position.BACKEND), skills);
        given(memberRepository.findByEmail(any()))
                .willReturn(Optional.of(member));

        //when
        //then
        assertThatThrownBy(() -> memberService.join(request))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void 로그인한_사용자_자신의_정보를_조회한다() throws MalformedURLException {
        // given
        final Email email = new Email("sidepair@email.com");
        final Password password = new Password("password1!");
        final Nickname nickname = new Nickname("nickname");
        final MemberSkills skills = new MemberSkills(List.of(new MemberSkill(1L, new SkillName("Java"))));
        final MemberImage memberImage = new MemberImage("originalFileName", "serverFilePath", ImageContentType.PNG);
        final Member member = new Member(1L, email, null, new EncryptedPassword(password), nickname, memberImage,
                new MemberProfile(Position.BACKEND), skills);

        given(memberRepository.findWithMemberProfileAndImageByEmail(any()))
                .willReturn(Optional.of(member));
        given(fileService.generateUrl(anyString(), any()))
                .willReturn(new URL("http://example.com/serverFilePath"));

        // when
        final MemberInformationResponse response = memberService.findMemberInformation(email.getValue());


        // then
        final MemberInformationResponse expected = new MemberInformationResponse(1L, "nickname",
                "http://example.com/serverFilePath", Position.BACKEND.name(),
                 List.of(new MemberSkillResponse(1L, "Java")),
                 "sidepair@email.com");

        assertThat(response).isEqualTo(expected);
    }

    @Test
    void 로그인한_사용자_자신의_정보를_조회할때_존재하지_않는_회원일_경우_예외가_발생한다() {
        // given
        final Email email = new Email("sidepair@email.com");

        given(memberRepository.findWithMemberProfileAndImageByEmail(any()))
                .willReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> memberService.findMemberInformation(email.getValue()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("존재하지 않는 회원입니다.");
    }

    @Test
    void 특정_사용자의_정보를_조회한다() throws MalformedURLException {
        // given
        final Email email = new Email("sidepair@email.com");
        final Password password = new Password("password1!");
        final Nickname nickname = new Nickname("nickname");
        final MemberSkills skills = new MemberSkills(List.of(new MemberSkill(1L, new SkillName("Java"))));
        final MemberImage memberImage = new MemberImage("originalFileName", "serverFilePath", ImageContentType.PNG);
        final Member member = new Member(email, new EncryptedPassword(password), nickname, memberImage,
                new MemberProfile(Position.BACKEND), skills);

        given(memberRepository.findWithMemberProfileAndImageById(any()))
                .willReturn(Optional.of(member));
        given(fileService.generateUrl(anyString(), any()))
                .willReturn(new URL("http://example.com/serverFilePath"));

        // when
        final MemberInformationForPublicResponse response = memberService.findMemberInformationForPublic(1L);

        // then
        final MemberInformationForPublicResponse expected = new MemberInformationForPublicResponse("nickname",
                "http://example.com/serverFilePath",
                Position.BACKEND.name(),
                List.of(new MemberSkillResponse(1L, "Java")));

        assertThat(response).isEqualTo(expected);
    }

    @Test
    void 특정_사용자의_정보를_조회할때_로그인한_사용자가_존재하지_않는_회원이면_예외가_발생한다() {
        // given
        // when
        // then
        assertThatThrownBy(() -> memberService.findMemberInformationForPublic(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("존재하지 않는 회원입니다.");
    }

    @Test
    void 특정_사용자의_정보를_조회할때_조회하려는_사용자가_존재하지_않는_회원이면_예외가_발생한다() throws MalformedURLException {
        // given
        final Email email = new Email("sidepair@email.com");
        final Password password = new Password("password1!");
        final Nickname nickname = new Nickname("nickname");
        final MemberSkills skills = new MemberSkills(List.of(new MemberSkill(1L, new SkillName("Java"))));
        final MemberImage memberImage = new MemberImage("originalFileName", "serverFilePath", ImageContentType.PNG);
        final Member member = new Member(email, new EncryptedPassword(password), nickname, memberImage,
                new MemberProfile(Position.BACKEND), skills);

        given(memberRepository.findWithMemberProfileAndImageById(any()))
                .willReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> memberService.findMemberInformationForPublic(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("존재하지 않는 회원입니다. memberId = 1");
    }

    @Test
    void oauth_회원가입을_한다() {
        //given
        final OauthMemberJoinDto request = new OauthMemberJoinDto("oauthId", "sidepair@email.com", PositionType.ETC,"nickname");

        given(memberRepository.save(any()))
                .willReturn(new Member(1L, null, null, null, null, null, null, null));
        given(environment.getProperty(IMAGE_DEFAULT_ORIGINAL_FILE_NAME_PROPERTY))
                .willReturn("default-member-image");
        given(environment.getProperty(IMAGE_DEFAULT_SERVER_FILE_PATH_PROPERTY))
                .willReturn("https://blog.kakaocdn.net/dn/nGKCL/btsF9BfyYTT/R0ERaU79wCKH2eHylGxK1k/img.png");
        given(environment.getProperty(IMAGE_DEFAULT_IMAGE_CONTENT_TYPE_PROPERTY))
                .willReturn("JPG");
        given(numberGenerator.generate())
                .willReturn(7);
        given(tokenProvider.createRefreshToken(any(), any()))
                .willReturn("refreshToken");
        given(tokenProvider.createAccessToken(any(), any()))
                .willReturn("accessToken");

        //when
        final AuthenticationResponse result = memberService.oauthJoin(request);

        //then
        assertThat(result).isEqualTo(new AuthenticationResponse("refreshToken", "accessToken"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"kkk", "kkkk"})
    void oauth_회원가입_시_이메일의_아이디와_UUID_길이의_합이_40이하일때_회원을_생성한다(final String value) {
        //given
        final OauthMemberJoinDto request = new OauthMemberJoinDto("oauthId", value + "@email.com", PositionType.ETC, "nickname");

        given(memberRepository.save(any()))
                .willReturn(new Member(1L, null, null, null, null, null, null, null));
        given(environment.getProperty(IMAGE_DEFAULT_ORIGINAL_FILE_NAME_PROPERTY))
                .willReturn("default-member-image");
        given(environment.getProperty(IMAGE_DEFAULT_SERVER_FILE_PATH_PROPERTY))
                .willReturn("https://blog.kakaocdn.net/dn/nGKCL/btsF9BfyYTT/R0ERaU79wCKH2eHylGxK1k/img.png");
        given(environment.getProperty(IMAGE_DEFAULT_IMAGE_CONTENT_TYPE_PROPERTY))
                .willReturn("JPG");
        given(numberGenerator.generate())
                .willReturn(7);
        given(tokenProvider.createRefreshToken(any(), any()))
                .willReturn("refreshToken");
        given(tokenProvider.createAccessToken(any(), any()))
                .willReturn("accessToken");

        //when
        final AuthenticationResponse result = memberService.oauthJoin(request);

        //then
        assertThat(result).isEqualTo(new AuthenticationResponse("refreshToken", "accessToken"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"kkkkk", "kkkkkk", "kkkkkkkkkkkkkkkkk"})
    void oauth_회원가입_시_이메일의_아이디와_UUID_길이의_합이_40초과일때_회원을_생성한다(final String value) {
        //given
        final OauthMemberJoinDto request = new OauthMemberJoinDto("oauthId", value + "@email.com", PositionType.ETC, "nickname");

        given(memberRepository.save(any()))
                .willReturn(new Member(1L, null, null, null, null, null, null,null));
        given(environment.getProperty(IMAGE_DEFAULT_ORIGINAL_FILE_NAME_PROPERTY))
                .willReturn("default-member-image");
        given(environment.getProperty(IMAGE_DEFAULT_SERVER_FILE_PATH_PROPERTY))
                .willReturn("https://blog.kakaocdn.net/dn/nGKCL/btsF9BfyYTT/R0ERaU79wCKH2eHylGxK1k/img.png");
        given(environment.getProperty(IMAGE_DEFAULT_IMAGE_CONTENT_TYPE_PROPERTY))
                .willReturn("JPG");
        given(numberGenerator.generate())
                .willReturn(7);
        given(tokenProvider.createRefreshToken(any(), any()))
                .willReturn("refreshToken");
        given(tokenProvider.createAccessToken(any(), any()))
                .willReturn("accessToken");

        //when
        final AuthenticationResponse result = memberService.oauthJoin(request);

        //then
        assertThat(result).isEqualTo(new AuthenticationResponse("refreshToken", "accessToken"));
    }
}
