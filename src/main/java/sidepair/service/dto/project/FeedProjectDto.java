package sidepair.service.dto.project;

import java.time.LocalDate;
import java.time.LocalDateTime;
import sidepair.domain.project.ProjectStatus;
import sidepair.service.dto.mamber.MemberDto;

public record FeedProjectDto(

        Long projectId,
        String name,
        ProjectStatus status,
        Integer currentMemberCount,
        Integer limitedMemberCount,
        LocalDateTime createdAt,
        LocalDate startDate,
        LocalDate endDate,
        MemberDto projectLeader
) {

}
