package sidepair.member.domain;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import sidepair.global.domain.BaseUpdatedTimeEntity;
import sidepair.member.domain.vo.Nickname;
import sidepair.member.domain.vo.Skill;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberProfile extends BaseUpdatedTimeEntity {
    @Column(length = 10, nullable = false)
    private Nickname nickname;

    @Column(length = 100, nullable = false)
    private String email;

    @Column(nullable = false)
    private Skill skills;

    public MemberProfile(final Nickname nickname, final String email) {
        this.nickname = nickname;
        this.email = email;
    }

    public Nickname getNickname() {
        return nickname;
    }

    public String getEmail() {
        return email;
    }

    public Skill getSkills() {
        return skills;
    }
}
