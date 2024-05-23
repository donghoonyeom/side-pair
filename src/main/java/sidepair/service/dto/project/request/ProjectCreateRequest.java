package sidepair.service.dto.project.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ProjectCreateRequest(

        @NotNull(message = "피드 컨텐츠 아이디는 빈 값일 수 없습니다.")
        Long feedContentId,

        @NotBlank(message = "프로젝트 이름을 빈 값일 수 없습니다.")
        String name,

        @NotNull(message = "프로젝트 제한 인원은 빈 값일 수 없습니다.")
        Integer limitedMemberCount,

        @Valid
        List<ProjectFeedNodeRequest> projectFeedNodeRequests
) {

}
