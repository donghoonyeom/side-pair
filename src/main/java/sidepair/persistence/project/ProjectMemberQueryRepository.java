package sidepair.persistence.project;

import java.util.List;
import java.util.Optional;
import sidepair.domain.member.vo.Email;
import sidepair.domain.project.ProjectMember;
import sidepair.persistence.project.dto.ProjectMemberSortType;

public interface ProjectMemberQueryRepository {

    List<ProjectMember> findByProjectIdOrderedBySortType(final Long projectId,
                                                         final ProjectMemberSortType sortType);

    Optional<ProjectMember> findProjectMember(final Long projectId, final Email memberEmail);
}
