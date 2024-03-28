package sidepair.domain.member;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import sidepair.domain.BaseEntity;
import sidepair.domain.member.vo.SkillName;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberSkill extends BaseEntity {

    @Embedded
    private SkillName name;

    public MemberSkill(final SkillName name) {
        this.name = name;
    }

    public MemberSkill(final Long id, final SkillName name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final MemberSkill that = (MemberSkill) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getId(), getName());
    }

    @Override
    public Long getId() {
        return id;
    }

    public SkillName getName() {
        return name;
    }
}
