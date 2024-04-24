package sidepair.domain.project;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import sidepair.domain.project.exeption.ProjectException;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectFeedNodes {
    private static final int DATE_OFFSET = 1;

    @OneToMany(fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
            orphanRemoval = true)
    @JoinColumn(name = "project_id", nullable = false, updatable = false)
    private final List<ProjectFeedNode> values = new ArrayList<>();

    public ProjectFeedNodes(final List<ProjectFeedNode> values) {
        final List<ProjectFeedNode> copiedValues = new ArrayList<>(values);
        validatePeriodNoOverlap(copiedValues);
        this.values.addAll(copiedValues);
    }

    public void validatePeriodNoOverlap(final List<ProjectFeedNode> nodes) {
        sortByStartDateAsc(nodes);

        IntStream.range(0, nodes.size() - 1)
                .filter(index -> nodes.get(index).isEndDateEqualOrAfterOtherStartDate(nodes.get(index + 1)))
                .findAny()
                .ifPresent(it -> {
                    throw new ProjectException("프로젝트 노드의 기간이 겹칠 수 없습니다.");
                });
    }

    private void sortByStartDateAsc(final List<ProjectFeedNode> nodes) {
        nodes.sort(Comparator.comparing(ProjectFeedNode::getStartDate));
    }

    public void addAll(final ProjectFeedNodes projectFeedNodes) {
        this.values.addAll(new ArrayList<>(projectFeedNodes.values));
    }

    public LocalDate getProjectStartDate() {
        return values.stream()
                .min(Comparator.comparing(ProjectFeedNode::getStartDate))
                .orElseThrow(() -> new ProjectException("프로젝트에 노드가 존재하지 않습니다."))
                .getStartDate();
    }

    public LocalDate getProjectEndDate() {
        return values.stream()
                .max(Comparator.comparing(ProjectFeedNode::getEndDate))
                .orElseThrow(() -> new ProjectException("프로젝트에 노드가 존재하지 않습니다."))
                .getEndDate();
    }

    public int addTotalPeriod() {
        return (int) ChronoUnit.DAYS.between(getProjectStartDate(), getProjectEndDate()) + DATE_OFFSET;
    }

    public Optional<ProjectFeedNode> getNodeByDate(final LocalDate date) {
        sortByStartDateAsc(values);

        return values.stream()
                .filter(node -> node.isDayOfNode(date))
                .findFirst();
    }

    public int size() {
        return values.size();
    }

    public boolean hasFrontNode(final ProjectFeedNode node) {
        sortByStartDateAsc(values);
        return values.indexOf(node) != 0;
    }

    public boolean hasBackNode(final ProjectFeedNode node) {
        sortByStartDateAsc(values);
        return values.indexOf(node) != (size() - 1);
    }

    public Optional<ProjectFeedNode> nextNode(final ProjectFeedNode projectNode) {
        sortByStartDateAsc(values);

        if (hasBackNode(projectNode)) {
            return Optional.of(values.get(values.indexOf(projectNode) + 1));
        }
        return Optional.empty();
    }

    public int calculateAllCheckCount() {
        return values.stream()
                .mapToInt(ProjectFeedNode::getCheckCount)
                .sum();
    }

    public List<ProjectFeedNode> getValues() {
        return new ArrayList<>(values);
    }
}
