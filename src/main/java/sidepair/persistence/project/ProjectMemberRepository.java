package sidepair.persistence.project;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sidepair.domain.member.vo.Email;
import sidepair.domain.project.Project;
import sidepair.domain.project.ProjectMember;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long>,
        ProjectMemberQueryRepository, ProjectMemberJdbcRepository {

    @Query("select gm from ProjectMember gm "
            + "inner join fetch gm.project g "
            + "inner join fetch gm.member m "
            + "where g=:project "
            + "and m.email =:email")
    Optional<ProjectMember> findByProjectAndMemberEmail(
            @Param("project") final Project project, @Param("email") final Email email);

    @Query("select gm from ProjectMember gm "
            + "join fetch gm.project g "
            + "join fetch gm.member m "
            + "where g=:project "
            + "and gm.member = m")
    List<ProjectMember> findAllByProject(@Param("project") final Project project);
}
