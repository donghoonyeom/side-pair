package sidepair.service.dto.auth;

import sidepair.domain.member.vo.Email;
import sidepair.domain.member.vo.Password;

public record LoginDto(
        Email email,
        Password password
) {

}
