package sidepair.service.dto.project.response;

import java.time.LocalDate;

public record MemoirResponse(
        Long id,
        String description,
        LocalDate createdAt
) {
}
