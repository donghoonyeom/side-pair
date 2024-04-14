package sidepair.service.dto.project.response;

import java.util.List;

public record ProjectFeedNodesResponse(
        boolean hasFrontNode,
        boolean hasBackNode,
        List<ProjectFeedNodeResponse> nodes
) {

}
