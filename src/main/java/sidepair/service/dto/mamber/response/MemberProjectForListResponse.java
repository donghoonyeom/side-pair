package sidepair.service.dto.mamber.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MemberProjectForListResponse(
        Long projectId,
        String name,
        String projectStatus,
        Integer currentMemberCount,
        Integer limitedMemberCount,
        LocalDateTime createdAt,
        LocalDate startDate,
        LocalDate endDate,
        MemberResponse projectLeader
) {

}
