package sidepair.persistence.project;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sidepair.domain.member.vo.Email;
import sidepair.domain.project.ProjectMember;
import sidepair.domain.project.ProjectToDo;
import sidepair.domain.project.ProjectToDoCheck;

public interface ProjectToDoCheckRepository extends JpaRepository<ProjectToDoCheck, Long> {

    @Query("select gc from ProjectToDoCheck gc "
            + "inner join fetch gc.projectMember gcm "
            + "inner join fetch gcm.member m "
            + "inner join fetch gcm.project g "
            + "where m.email = :email "
            + "and gc.projectToDo = :projectTodo "
            + "and g.id = :projectId")
    Optional<ProjectToDoCheck> findByProjectIdAndTodoAndMemberEmail(
            @Param("projectId") final Long projectId,
            @Param("projectTodo") final ProjectToDo projectToDo,
            @Param("email") final Email email);

    @Query("select gc from ProjectToDoCheck gc "
            + "inner join fetch gc.projectMember gcm "
            + "inner join fetch gcm.member m "
            + "inner join fetch gcm.project g "
            + "where m.email = :email "
            + "and g.id = :projectId ")
    List<ProjectToDoCheck> findByProjectIdAndMemberEmail(
            @Param("projectId") final Long projectId,
            @Param("email") final Email email);

    @Modifying
    @Query("delete from ProjectToDoCheck gc "
            + "where gc.projectMember = :projectMember "
            + "and gc.projectToDo.id = :todoId")
    void deleteByProjectMemberAndToDoId(@Param("projectMember") final ProjectMember projectMember,
                                        @Param("todoId") final Long todoId);
}
