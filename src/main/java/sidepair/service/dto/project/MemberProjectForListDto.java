package sidepair.service.dto.project;

import java.time.LocalDate;
import java.time.LocalDateTime;
import sidepair.service.dto.mamber.MemberDto;

public record MemberProjectForListDto(
        Long projectId,
        String name,
        String projectStatus,
        Integer currentMemberCount,
        Integer limitedMemberCount,
        LocalDateTime createdAt,
        LocalDate startDate,
        LocalDate endDate,
        MemberDto projectLeader
) {

}
