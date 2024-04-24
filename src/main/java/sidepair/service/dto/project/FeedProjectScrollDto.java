package sidepair.service.dto.project;

import java.util.List;

public record FeedProjectScrollDto(
        List<FeedProjectDto> feedProjectDtos,
        boolean hasNext
) {

}
