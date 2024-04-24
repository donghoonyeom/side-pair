package sidepair.service.dto.mamber.response;

import java.time.LocalDate;
import java.util.List;
import sidepair.service.dto.project.response.MemoirResponse;
import sidepair.service.dto.project.response.ProjectFeedNodesResponse;
import sidepair.service.dto.project.response.ProjectTodoResponse;

public record MemberProjectResponse(
        String name,
        String status,
        Long leaderId,
        Integer currentMemberCount,
        Integer limitedMemberCount,
        LocalDate startDate,
        LocalDate endDate,
        Long feedContentId,
        ProjectFeedNodesResponse projectFeedNodes,
        List<ProjectTodoResponse> projectTodos,
        List<MemoirResponse> memoirs
) {

}