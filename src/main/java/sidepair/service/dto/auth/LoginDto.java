package sidepair.service.dto.auth;

import sidepair.member.domain.vo.Email;
import sidepair.member.domain.vo.Password;

public record LoginDto(
        Email email,
        Password password
) {

}
