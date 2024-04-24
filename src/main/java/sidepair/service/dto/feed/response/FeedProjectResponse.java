package sidepair.service.dto.feed.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import sidepair.domain.project.ProjectStatus;
import sidepair.service.dto.mamber.response.MemberResponse;

public record FeedProjectResponse(
        Long projectId,
        String name,
        ProjectStatus status,
        Integer currentMemberCount,
        Integer limitedMemberCount,
        LocalDateTime createdAt,
        LocalDate startDate,
        LocalDate endDate,
        MemberResponse projectLeader
) {

}
