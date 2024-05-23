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

    @Query("select pc from ProjectToDoCheck pc "
            + "inner join fetch pc.projectMember pcm "
            + "inner join fetch pcm.member m "
            + "inner join fetch pcm.project p "
            + "where m.email = :email "
            + "and pc.projectToDo = :projectTodo "
            + "and p.id = :projectId")
    Optional<ProjectToDoCheck> findByProjectIdAndTodoAndMemberEmail(
            @Param("projectId") final Long projectId,
            @Param("projectTodo") final ProjectToDo projectToDo,
            @Param("email") final Email email);

    @Query("select pc from ProjectToDoCheck pc "
            + "inner join fetch pc.projectMember pcm "
            + "inner join fetch pcm.member m "
            + "inner join fetch pcm.project p "
            + "where m.email = :email "
            + "and p.id = :projectId ")
    List<ProjectToDoCheck> findByProjectIdAndMemberEmail(
            @Param("projectId") final Long projectId,
            @Param("email") final Email email);

    @Modifying
    @Query("delete from ProjectToDoCheck pc "
            + "where pc.projectMember = :projectMember "
            + "and pc.projectToDo.id = :todoId")
    void deleteByProjectMemberAndToDoId(@Param("projectMember") final ProjectMember projectMember,
                                        @Param("todoId") final Long todoId);
}
