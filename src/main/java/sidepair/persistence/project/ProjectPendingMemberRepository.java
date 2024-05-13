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

    @Query("select pp from ProjectPendingMember pp "
            + "inner join fetch pp.project p "
            + "inner join fetch pp.member m "
            + "where p=:project "
            + "and m.email =:email")
    Optional<ProjectPendingMember> findByProjectAndMemberEmail(
            @Param("project") final Project project, @Param("email") final Email email);

    List<ProjectPendingMember> findByProject(final Project project);

    @Query("select pp from ProjectPendingMember pp "
            + "join fetch pp.project p "
            + "join fetch pp.member m "
            + "where p=:project "
            + "and pp.member = m")
    List<ProjectPendingMember> findAllByProject(@Param("project") final Project project);

    @Modifying
    @Query("DELETE FROM ProjectPendingMember pp WHERE pp.id IN :ids")
    void deleteAllByIdIn(@Param("ids") final List<Long> ids);
}
