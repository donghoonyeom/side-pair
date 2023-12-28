package sidepair.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.LinkedHashSet;
import java.util.Set;
import sidepair.global.domain.BaseEntity;
import sidepair.member.domain.vo.Email;
import sidepair.member.domain.vo.Nickname;
import sidepair.member.domain.vo.ProfileImgUrl;
import sidepair.member.domain.vo.Skill;

public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Nickname nickname;

    @Embedded
    private Email email;

    @Embedded
    private ProfileImgUrl profileImgUrl;

    @Column
    private Set<Skill> skills;

    public Member(
            final Nickname nickname,
            final Email email,
            final ProfileImgUrl profileImgUrl
    ) {
        this.nickname = nickname;
        this.email = email;
        this.profileImgUrl = profileImgUrl;
        this.skills = new LinkedHashSet<>(5);
    }

    public Member(
            final String nickname,
            final String email,
            final String profileImgUrl
    ) {
        this(new Nickname(nickname), new Email(email), new ProfileImgUrl(profileImgUrl));
    }



}
