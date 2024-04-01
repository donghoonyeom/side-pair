package sidepair.service.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import sidepair.domain.member.vo.Email;
import sidepair.domain.member.vo.Password;
import sidepair.service.dto.auth.LoginDto;
import sidepair.service.dto.auth.request.LoginRequest;
import sidepair.service.dto.auth.response.AuthenticationResponse;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthMapper {

    public static LoginDto convertToLoginDto(final LoginRequest request) {
        return new LoginDto(new Email(request.email()), new Password(request.password()));
    }

    public static AuthenticationResponse convertToAuthenticationResponse(final String refreshToken,
                                                                         final String accessToken) {
        return new AuthenticationResponse(refreshToken, accessToken);
    }
}
