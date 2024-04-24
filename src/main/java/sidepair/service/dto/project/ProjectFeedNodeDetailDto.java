package sidepair.service.dto.project;

import java.time.LocalDate;
import java.util.List;

public record ProjectFeedNodeDetailDto(
        Long id,
        String title,
        String description,
        List<String> imageUrls,
        LocalDate startDate,
        LocalDate endDate,
        Integer checkCount
) {

}

