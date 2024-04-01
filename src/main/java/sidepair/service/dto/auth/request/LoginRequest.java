package sidepair.service.dto.auth.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "이메일은 빈 값일 수 없습니다.")
        String email,

        @NotBlank(message = "비밀번호는 빈 값일 수 없습니다.")
        String password
) {

}

