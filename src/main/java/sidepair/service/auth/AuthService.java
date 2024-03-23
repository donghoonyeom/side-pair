package sidepair.service.auth;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sidepair.member.domain.Member;
import sidepair.member.domain.vo.Email;
import sidepair.member.domain.vo.Password;
import sidepair.persistence.auth.RefreshTokenRepository;
import sidepair.persistence.member.MemberRepository;
import sidepair.service.aop.ExceptionConvert;
import sidepair.service.dto.auth.LoginDto;
import sidepair.service.dto.auth.request.LoginRequest;
import sidepair.service.dto.auth.request.ReissueTokenRequest;
import sidepair.service.dto.auth.response.AuthenticationResponse;
import sidepair.service.exception.AuthenticationException;
import sidepair.service.mapper.AuthMapper;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@ExceptionConvert
public class AuthService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;

    @Transactional
    public AuthenticationResponse login(final LoginRequest loginRequest) {
        final LoginDto loginDto = AuthMapper.convertToLoginDto(loginRequest);
        final Member member = findMember(loginDto);
        checkPassword(loginDto.password(), member);
        return makeAuthenticationResponse(member);
    }

    private Member findMember(final LoginDto loginDto) {
        return memberRepository.findByEmail(loginDto.email())
                .orElseThrow(() -> new AuthenticationException("존재하지 않는 이메일입니다."));
    }

    private void checkPassword(final Password password, final Member member) {
        if (member.isPasswordMismatch(password)) {
            throw new AuthenticationException("비밀번호가 일치하지 않습니다.");
        }
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

    public AuthenticationResponse oauthLogin(final Member member) {
        return makeAuthenticationResponse(member);
    }

    public boolean isCertified(final String token) {
        return tokenProvider.isValidToken(token);
    }

    @Transactional
    public AuthenticationResponse reissueToken(final ReissueTokenRequest reissueTokenRequest) {
        checkTokenValid(reissueTokenRequest.refreshToken());
        final String memberEmail = findMemberEmailByRefreshToken(reissueTokenRequest.refreshToken());
        final Member member = findMemberByRefreshToken(memberEmail);
        return makeAuthenticationResponse(member);
    }

    private void checkTokenValid(final String token) {
        if (!isCertified(token)) {
            throw new AuthenticationException("토큰이 유효하지 않습니다.");
        }
    }

    private String findMemberEmailByRefreshToken(final String clientRefreshToken) {
        return refreshTokenRepository.findMemberEmailByRefreshToken(clientRefreshToken)
                .orElseThrow(() -> new AuthenticationException("토큰이 만료 되었습니다."));
    }

    private Member findMemberByRefreshToken(final String memberEmail) {
        return memberRepository.findByEmail(new Email(memberEmail))
                .orElseThrow(() -> new AuthenticationException("존재하지 않는 회원입니다."));
    }

    public String findEmailByToken(final String token) {
        return tokenProvider.findSubject(token);
    }
}
