package sidepair.domain.project;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectToDos {

    @OneToMany(fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
            orphanRemoval = true)
    @JoinColumn(name = "project_id", updatable = false, nullable = false)
    private final List<ProjectToDo> values = new ArrayList<>();

    public ProjectToDos(final List<ProjectToDo> values) {
        this.values.addAll(new ArrayList<>(values));
    }

    public void add(final ProjectToDo projectToDo) {
        values.add(projectToDo);
    }

    public ProjectToDo findLast() {
        return values.get(values.size() - 1);
    }

    public Optional<ProjectToDo> findById(final Long todoId) {
        return values.stream()
                .filter(projectToDo -> projectToDo.isSameId(todoId))
                .findFirst();
    }

    public List<ProjectToDo> getValues() {
        return new ArrayList<>(values);
    }
}
