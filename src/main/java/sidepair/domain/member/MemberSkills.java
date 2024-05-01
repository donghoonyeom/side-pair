package sidepair.domain.member;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import sidepair.domain.member.vo.SkillName;
import sidepair.domain.member.exception.MemberException;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberSkills {

    private static final int MIN_COUNT = 1;
    private static final int MAX_COUNT = 5;

    @OneToMany(fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
            orphanRemoval = true)
    @JoinColumn(name = "member_id", nullable = false)
    @BatchSize(size = 20)
    private final Set<MemberSkill> values = new HashSet<>();

    public MemberSkills(final List<MemberSkill> memberSkills) {
        validate(memberSkills);
        values.addAll(new HashSet<>(memberSkills));
    }

    private void validate(final List<MemberSkill> memberSkills) {
        validateCount(memberSkills);
        validateDuplicatedName(memberSkills);
    }

    private void validateCount(final List<MemberSkill> memberSkills) {
        if (memberSkills.size() < MIN_COUNT || memberSkills.size() > MAX_COUNT) {
            throw new MemberException(
                    String.format("기술의 개수는 최소 %d개부터 최대 %d개까지 가능합니다.", MIN_COUNT, MAX_COUNT));
        }
    }

    private void validateDuplicatedName(final List<MemberSkill> memberSkills) {
        final Set<SkillName> nonDuplicatedNames = memberSkills.stream()
                .map(MemberSkill::getName)
                .collect(Collectors.toSet());
        if (memberSkills.size() != nonDuplicatedNames.size()) {
            throw new MemberException("기술 이름은 중복될 수 없습니다.");
        }
    }

    public void addAll(final MemberSkills memberSkills) {
        this.values.addAll(new HashSet<>(memberSkills.values));
    }

    public Set<MemberSkill> getValues() {
        return new HashSet<>(values);
    }
}
