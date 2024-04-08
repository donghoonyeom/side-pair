package sidepair.domain.project;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import sidepair.domain.BaseEntity;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectToDoCheck extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_member_id", nullable = false)
    private ProjectMember projectMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_to_do_id", nullable = false)
    private ProjectToDo projectToDo;

    public ProjectToDoCheck(final ProjectMember projectMember, final ProjectToDo projectToDo) {
        this.projectMember = projectMember;
        this.projectToDo = projectToDo;
    }

    public ProjectToDo getProjectToDo() {
        return projectToDo;
    }
}
