package sidepair.service.dto.project.response;

import java.time.LocalDate;

public record ProjectTodoResponse(
        Long id,
        String content,
        LocalDate startDate,
        LocalDate endDate,
        ProjectToDoCheckResponse check
) {

}