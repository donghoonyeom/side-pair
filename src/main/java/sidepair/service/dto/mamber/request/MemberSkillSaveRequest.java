package sidepair.service.dto.mamber.request;

import jakarta.validation.constraints.Pattern;

public record MemberSkillSaveRequest(
        @Pattern(regexp = "^[a-zA-Z가-힣]{1,10}$", message = "형식에 맞지 않는 기술 이름입니다.")
        String name
) {

}
