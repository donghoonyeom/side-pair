package sidepair.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sidepair.domain.member.EncryptedPassword;
import sidepair.domain.member.Member;
import sidepair.domain.member.MemberProfile;
import sidepair.domain.member.MemberSkill;
import sidepair.domain.member.MemberSkills;
import sidepair.domain.member.Position;
import sidepair.domain.member.vo.Email;
import sidepair.domain.member.vo.Nickname;
import sidepair.domain.member.vo.Password;
import sidepair.domain.member.vo.SkillName;
import sidepair.persistence.auth.RefreshTokenRepository;
import sidepair.persistence.member.MemberRepository;
import sidepair.service.auth.AuthService;
import sidepair.service.auth.TokenProvider;
import sidepair.service.dto.auth.request.LoginRequest;
import sidepair.service.dto.auth.request.ReissueTokenRequest;
import sidepair.service.dto.auth.response.AuthenticationResponse;
import sidepair.service.exception.AuthenticationException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static Member member;

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private AuthService authService;

    @BeforeAll
    static void setUp() {
        final Email email = new Email("user@example.com");
        final Password password = new Password("password1!");
        final EncryptedPassword encryptedPassword = new EncryptedPassword(password);
        final Nickname nickname = new Nickname("nickname");
        final MemberSkills skills = new MemberSkills(
                List.of(new MemberSkill(1L, new SkillName("Java"))));
        final MemberProfile memberProfile = new MemberProfile(Position.BACKEND);
        member = new Member(email, encryptedPassword, nickname, null, memberProfile, skills);
    }

    @Test
    void 정상적으로_로그인을_한다() {
        //given
        final LoginRequest loginRequest = new LoginRequest("user@example.com", "password1!");
        final String accessToken = "accessToken";
        final String refreshToken = "refreshToken";

        given(memberRepository.findByEmail(any()))
                .willReturn(Optional.of(member));
        given(tokenProvider.createAccessToken(any(), any()))
                .willReturn(accessToken);
        given(tokenProvider.createRefreshToken(any(), any()))
                .willReturn(refreshToken);

        //when
        final AuthenticationResponse authenticationResponse = authService.login(loginRequest);

        //then
        assertThat(authenticationResponse).isEqualTo(
                new AuthenticationResponse(refreshToken, accessToken));
    }

    @Test
    void 존재하지_않는_이메일로_로그인_하는_경우_예외를_던진다() {
        //given
        final LoginRequest loginRequest = new LoginRequest("user@example.com", "password1!");
        given(memberRepository.findByEmail(any()))
                .willReturn(Optional.empty());

        //when
        //then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void 일치하지_않는_비밀번호로_로그인_하는_경우_예외를_던진다() {
        //given
        final LoginRequest loginRequest = new LoginRequest("user@example.com", "wrongpassword1!");
        given(memberRepository.findByEmail(any()))
                .willReturn(Optional.of(member));

        //when
        //then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void 정상적으로_토큰을_재발행한다() {
        //given
        final String accessToken = "accessToken";
        final String refreshToken = "refreshToken";
        final ReissueTokenRequest reissueTokenRequest = new ReissueTokenRequest("refreshToken");

        given(tokenProvider.isValidToken(any()))
                .willReturn(true);
        given(tokenProvider.createAccessToken(any(), any()))
                .willReturn(accessToken);
        given(tokenProvider.createRefreshToken(any(), any()))
                .willReturn(refreshToken);
        given(memberRepository.findByEmail(any()))
                .willReturn(Optional.of(member));
        given(refreshTokenRepository.findMemberEmailByRefreshToken(any()))
                .willReturn(Optional.of(member.getEmail().getValue()));

        //when
        final AuthenticationResponse authenticationResponse = authService.reissueToken(reissueTokenRequest);

        //then
        assertThat(authenticationResponse).isEqualTo(new AuthenticationResponse(refreshToken, accessToken));
    }

    @Test
    void 리프레시_토큰이_유효하지_않을_경우_예외를_던진다() {
        //given
        final String refreshToken = "refreshToken";
        final ReissueTokenRequest reissueTokenRequest = new ReissueTokenRequest(refreshToken);
        given(tokenProvider.isValidToken(any()))
                .willReturn(false);

        //when
        //then
        assertThatThrownBy(() -> authService.reissueToken(reissueTokenRequest))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void 리프레시_토큰이_만료_됐을_경우_예외를_던진다() {
        //given
        final String refreshToken = "refreshToken";
        final ReissueTokenRequest reissueTokenRequest = new ReissueTokenRequest(refreshToken);
        given(tokenProvider.isValidToken(any()))
                .willReturn(true);
        given(refreshTokenRepository.findMemberEmailByRefreshToken(any()))
                .willReturn(Optional.empty());

        //when
        //then
        assertThatThrownBy(() -> authService.reissueToken(reissueTokenRequest))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void 리프레시_토큰으로_조회한_회원이_존재하지_않는_경우_예외를_던진다() {
        //given
        final String refreshToken = "refreshToken";
        final ReissueTokenRequest reissueTokenRequest = new ReissueTokenRequest(refreshToken);
        given(tokenProvider.isValidToken(any()))
                .willReturn(true);
        given(refreshTokenRepository.findMemberEmailByRefreshToken(any()))
                .willReturn(Optional.of(member.getEmail().getValue()));
        given(memberRepository.findByEmail(any()))
                .willReturn(Optional.empty());

        //when
        //then
        assertThatThrownBy(() -> authService.reissueToken(reissueTokenRequest))
                .isInstanceOf(AuthenticationException.class);
    }
}