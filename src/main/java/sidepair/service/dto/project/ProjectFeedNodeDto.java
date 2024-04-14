package sidepair.service.dto.project;

import java.time.LocalDate;

public record ProjectFeedNodeDto(
        Long feedNodeId,
        int checkCount,
        LocalDate startDate,
        LocalDate endDate
) {

}
