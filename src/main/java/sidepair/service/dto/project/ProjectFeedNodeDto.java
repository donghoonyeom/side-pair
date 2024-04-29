package sidepair.service.dto.project;

import java.time.LocalDate;

public record ProjectFeedNodeDto(
        Long feedNodeId,
        int memoirCount,
        LocalDate startDate,
        LocalDate endDate
) {

}
