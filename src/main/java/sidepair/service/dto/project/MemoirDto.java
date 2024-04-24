package sidepair.service.dto.project;

import java.time.LocalDateTime;

public record MemoirDto(
        Long id,
        String description,
        LocalDateTime createdAt
) {
}
