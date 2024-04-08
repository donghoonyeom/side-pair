package sidepair.domain.project;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import sidepair.domain.exception.UnexpectedDomainException;
import sidepair.domain.member.Member;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectMembers {

    private static final int MIN_SIZE_TO_FIND_NEXT_LEADER = 1;

    @BatchSize(size = 20)
    @OneToMany(fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
            orphanRemoval = true, mappedBy = "project")
    private final List<ProjectMember> values = new ArrayList<>();

    public ProjectMembers(final List<ProjectMember> values) {
        this.values.addAll(new ArrayList<>(values));
    }

    public void add(final ProjectMember projectMember) {
        this.values.add(projectMember);
    }

    public void addAll(final List<ProjectMember> projectMembers) {
        this.values.addAll(new ArrayList<>(projectMembers));
    }

    public Optional<ProjectMember> findByMember(final Member member) {
        return values.stream()
                .filter(value -> value.isSameMember(member))
                .findFirst();
    }

    public Optional<ProjectMember> findNextLeader() {
        if (size() <= MIN_SIZE_TO_FIND_NEXT_LEADER) {
            return Optional.empty();
        }
        values.sort(Comparator.comparing(ProjectMember::getJoinedAt));
        return Optional.of(values.get(1));
    }

    public boolean isMember(final Member member) {
        return values.stream()
                .anyMatch(value -> value.isSameMember(member));
    }

    public boolean isNotLeader(final Member member) {
        final Member projectLeader = findProjectLeader();
        return !projectLeader.equals(member);
    }

    public Member findProjectLeader() {
        return values.stream()
                .filter(ProjectMember::isLeader)
                .findFirst()
                .map(ProjectMember::getMember)
                .orElseThrow(() -> new UnexpectedDomainException("프로젝트의 리더가 없습니다."));
    }

    public int size() {
        return values.size();
    }

    public void remove(final ProjectMember projectMember) {
        values.remove(projectMember);
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public List<ProjectMember> getValues() {
        return values;
    }
}
