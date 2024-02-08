package sidepair.member.domain;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import sidepair.global.domain.BaseUpdatedTimeEntity;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberProfile extends BaseUpdatedTimeEntity {

    @Column(nullable = false)
    private Skill skills;

    public MemberProfile(Skill skills) {
        this.skills = skills;
    }


    public Skill getSkills() {
        return skills;
    }
}
