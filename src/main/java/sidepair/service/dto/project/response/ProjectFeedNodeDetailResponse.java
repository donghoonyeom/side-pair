package sidepair.service.dto.project.response;

import java.time.LocalDate;
import java.util.List;

public record ProjectFeedNodeDetailResponse(
        Long id,
        String title,
        String description,
        List<String> imageUrls,
        LocalDate startDate,
        LocalDate endDate,
        Integer checkCount
) {

}
