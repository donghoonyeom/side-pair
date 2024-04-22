package sidepair.persistence.project;

import java.util.List;
import sidepair.domain.project.Memoir;
import sidepair.domain.project.Project;
import sidepair.domain.project.ProjectFeedNode;

public interface MemoirQueryRepository {

    List<Memoir> findByRunningProjectFeedNodeWithMemberAndMemberImage(final ProjectFeedNode projectFeedNode);

    List<Memoir> findByRunningProjectFeedNode(final ProjectFeedNode currentProjectFeedNode);

    List<Memoir> findByProjectWithMemberAndMemberImage(final Project project);
}
