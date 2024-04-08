package sidepair.service.dto.project;

import java.time.LocalDateTime;

public record CheckFeedDto(
        Long id,
        String description,
        LocalDateTime createdAt
) {
}
