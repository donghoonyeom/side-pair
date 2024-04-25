package sidepair.persistence.project;

import java.util.List;
import sidepair.domain.project.ProjectPendingMember;
import sidepair.persistence.project.dto.ProjectMemberSortType;

public interface ProjectPendingMemberQueryRepository {

    List<ProjectPendingMember> findByProjectIdOrderedBySortType(final Long projectId,
                                                                final ProjectMemberSortType sortType);
}
