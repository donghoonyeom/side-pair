package sidepair.member.configuration.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import sidepair.service.dto.mamber.request.PositionType;

public record MemberJoinRequest(
        @NotBlank(message = "이메일은 빈 값일 수 없습니다.")
        @Email(message = "이메일 형식이 아닙니다.")
        String email,

        @NotBlank(message = "비밀번호는 빈 값일 수 없습니다.")
        String password,

        @NotBlank(message = "닉네임은 빈 값일 수 없습니다.")
        String nickname,

        @NotNull(message = "포지션은 빈 값일 수 없습니다.")
        PositionType positionType,

        @NotNull(message = "기술은 빈 값일 수 없습니다.")
        List<MemberSkillSaveRequest> skills
) {
}
