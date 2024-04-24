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
public class ProjectPendingMembers {

    private static final int MIN_SIZE_TO_FIND_NEXT_LEADER = 1;

    @BatchSize(size = 20)
    @OneToMany(fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
            orphanRemoval = true, mappedBy = "project")
    private final List<ProjectPendingMember> values = new ArrayList<>();

    public ProjectPendingMembers(final List<ProjectPendingMember> values) {
        this.values.addAll(new ArrayList<>(values));
    }

    public void add(final ProjectPendingMember projectPendingMember) {
        values.add(projectPendingMember);
    }

    public boolean containProjectPendingMember(final ProjectPendingMember projectPendingMember) {
        return values.stream()
                .anyMatch(value -> value.isSameMember(projectPendingMember.getMember()));
    }

    public boolean isMember(final Member member) {
        return values.stream()
                .anyMatch(value -> value.isSameMember(member));
    }

    public int size() {
        return values.size();
    }

    public Member findProjectLeader() {
        return values.stream()
                .filter(ProjectPendingMember::isLeader)
                .findFirst()
                .map(ProjectPendingMember::getMember)
                .orElseThrow(() -> new UnexpectedDomainException("프로잭트의 리더가 없습니다."));
    }

    public boolean isNotLeader(final Member member) {
        final Member projectLeader = findProjectLeader();
        return !projectLeader.equals(member);
    }

    public Optional<ProjectPendingMember> findByMember(final Member member) {
        return values.stream()
                .filter(value -> value.isSameMember(member))
                .findFirst();
    }

    public Optional<ProjectPendingMember> findNextLeader() {
        if (size() <= MIN_SIZE_TO_FIND_NEXT_LEADER) {
            return Optional.empty();
        }
        values.sort(Comparator.comparing(ProjectPendingMember::getJoinedAt));
        return Optional.of(values.get(1));
    }

    public void remove(final ProjectPendingMember projectPendingMember) {
        values.remove(projectPendingMember);
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public void deleteAll() {
        this.values.clear();
    }

    public List<ProjectPendingMember> getValues() {
        return new ArrayList<>(values);
    }
}
