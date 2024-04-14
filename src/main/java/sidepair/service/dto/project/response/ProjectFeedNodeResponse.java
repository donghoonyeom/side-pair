package sidepair.service.dto.project.response;

import java.time.LocalDate;

public record ProjectFeedNodeResponse(
        Long id,
        String title,
        LocalDate startDate,
        LocalDate endDate,
        Integer checkCount
) {

}
