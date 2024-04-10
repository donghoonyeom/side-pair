package sidepair.service.dto.project.response;

import java.time.LocalDate;

public record CheckFeedResponse(
        Long id,
        String description,
        LocalDate createdAt
) {
}
