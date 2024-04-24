package sidepair.service.dto.project;


import java.util.List;
import sidepair.domain.project.vo.LimitedMemberCount;
import sidepair.domain.project.vo.ProjectName;

public record ProjectCreateDto(
        Long feedContentId,
        ProjectName projectName,
        LimitedMemberCount limitedMemberCount,
        List<ProjectFeedNodeDto> projectFeedNodeDtos
) {

    public int projectFeedNodeDtosSize() {
        return projectFeedNodeDtos.size();
    }
}
