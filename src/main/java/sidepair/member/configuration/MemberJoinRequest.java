package sidepair.member.configuration;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import sidepair.member.domain.vo.Skill;

public record MemberJoinRequest(
        @NotBlank(message = "이메일은 빈 값일 수 없습니다.")
        @Email(message = "이메일 형식이 아닙니다.")
        String email,

        @NotBlank(message = "비밀번호는 빈 값일 수 없습니다.")
        String password,

        @NotBlank(message = "닉네임 빈 값일 수 없습니다.")
        String nickname,

        @NotBlank(message = "기술은 빈 값일 수 없습니다.")
        Skill skills
) {
}
