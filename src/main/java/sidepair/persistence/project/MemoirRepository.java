package sidepair.persistence.project;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import sidepair.domain.project.Memoir;
import sidepair.domain.project.Project;
import sidepair.domain.project.ProjectFeedNode;
import sidepair.domain.project.ProjectMember;

public interface MemoirRepository extends JpaRepository<Memoir, Long>, MemoirQueryRepository {

    @Query("SELECT cf"
            + " FROM Memoir cf"
            + " WHERE cf.projectMember = :projectMember"
            + " AND cf.createdAt >= :start"
            + " AND cf.createdAt < :end")
    Optional<Memoir> findByProjectMemberAndDateTime(final ProjectMember projectMember, final LocalDateTime start,
                                                    final LocalDateTime end);

    @Query("SELECT COUNT(cf)"
            + " FROM Memoir cf"
            + " WHERE cf.projectMember = :projectMember")
    int countByProjectMember(final ProjectMember projectMember);

    @Query("SELECT COUNT(cf)"
            + " FROM Memoir cf"
            + " WHERE cf.projectMember = :projectMember"
            + " AND cf.projectFeedNode = :projectFeedNode")
    int countByProjectMemberAndProjectFeedNode(final ProjectMember projectMember,
    final ProjectFeedNode projectFeedNode);

    @Query("SELECT cf"
            + " FROM Memoir cf"
            + " WHERE cf.projectMember.project =:project"
            + " ORDER BY cf.createdAt DESC")
    List<Memoir> findByProject(final Project project);

    List<Memoir> findByProjectFeedNode(final ProjectFeedNode projectFeedNode);
}
