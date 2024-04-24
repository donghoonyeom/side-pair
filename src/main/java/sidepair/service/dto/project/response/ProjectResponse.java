package sidepair.service.dto.project.response;

import java.util.List;

public record ProjectResponse(
        String name,
        Integer currentMemberCount,
        Integer limitedMemberCount,
        List<ProjectFeedNodeResponse> projectNodes,
        int period
) {

}
