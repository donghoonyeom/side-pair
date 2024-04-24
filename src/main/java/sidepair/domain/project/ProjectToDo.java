package sidepair.domain.project;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import sidepair.domain.BaseUpdatedTimeEntity;
import sidepair.domain.project.vo.Period;
import sidepair.domain.project.vo.ProjectTodoContent;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectToDo extends BaseUpdatedTimeEntity {

    @Embedded
    private ProjectTodoContent content;

    @Embedded
    private Period period;

    public ProjectToDo(final ProjectTodoContent content, final Period period) {
        this(null, content, period);
    }

    public ProjectToDo(final Long id, final ProjectTodoContent content, final Period period) {
        this.id = id;
        this.content = content;
        this.period = period;
    }

    public boolean isSameId(final Long todoId) {
        return this.id.equals(todoId);
    }

    public String getContent() {
        return content.getValue();
    }

    public LocalDate getStartDate() {
        return period.getStartDate();
    }

    public LocalDate getEndDate() {
        return period.getEndDate();
    }
}
