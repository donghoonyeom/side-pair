package sidepair.persistence.project;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sidepair.domain.member.vo.Email;
import sidepair.domain.project.Project;
import sidepair.domain.project.ProjectPendingMember;

public interface ProjectPendingMemberRepository extends JpaRepository<ProjectPendingMember, Long>,
        ProjectPendingMemberQueryRepository {

    @Query("select gp from ProjectPendingMember gp "
            + "inner join fetch gp.project g "
            + "inner join fetch gp.member m "
            + "where g=:project "
            + "and m.email =:email")
    Optional<ProjectPendingMember> findByProjectAndMemberEmail(
            @Param("project") final Project project, @Param("email") final Email email);

    List<ProjectPendingMember> findByProject(final Project project);

    @Query("select gp from ProjectPendingMember gp "
            + "join fetch gp.project g "
            + "join fetch gp.member m "
            + "where g=:project "
            + "and gp.member = m")
    List<ProjectPendingMember> findAllByProject(@Param("project") final Project project);

    @Modifying
    @Query("DELETE FROM ProjectPendingMember gp WHERE gp.id IN :ids")
    void deleteAllByIdIn(@Param("ids") final List<Long> ids);
}
