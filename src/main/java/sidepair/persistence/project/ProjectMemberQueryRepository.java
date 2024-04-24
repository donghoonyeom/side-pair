package sidepair.persistence.project;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;
import sidepair.domain.member.vo.Email;
import sidepair.domain.project.ProjectMember;
import sidepair.domain.project.ProjectStatus;
import sidepair.persistence.project.dto.ProjectMemberSortType;

public interface ProjectMemberQueryRepository {

    Optional<ProjectMember> findByFeedIdAndMemberEmailAndProjectStatus(
            @Param("feedId") final Long feedId,
            @Param("email") final Email email,
            @Param("status") final ProjectStatus status);

    List<ProjectMember> findByProjectIdOrderedBySortType(final Long projectId,
                                                         final ProjectMemberSortType sortType);

    Optional<ProjectMember> findProjectMember(final Long projectId, final Email memberEmail);
}
