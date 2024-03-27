package sidepair.member.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import sidepair.global.domain.BaseUpdatedTimeEntity;
import sidepair.member.domain.vo.Email;
import sidepair.member.domain.vo.MemberImage;
import sidepair.member.domain.vo.Nickname;
import sidepair.member.domain.vo.Password;


@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseUpdatedTimeEntity {

    @Embedded
    private Nickname nickname;

    @Embedded
    private Email email;

    private String oauthId;

    @Embedded
    private EncryptedPassword encryptedPassword;

    @Embedded
    private MemberSkills skills;

    @OneToOne(fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
            orphanRemoval = true)
    @JoinColumn(name = "member_image_id")
    private MemberImage image;

    @OneToOne(fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
            orphanRemoval = true)
    @JoinColumn(name = "member_profile_id", nullable = false, unique = true)
    private MemberProfile memberProfile;

    public Member(final Email email, final EncryptedPassword encryptedPassword, final Nickname nickname,
                  final MemberImage image, final MemberProfile memberProfile, final MemberSkills skills) {
        this(null, email, null, encryptedPassword, nickname, image, memberProfile, skills);
    }

    public Member(final Email email, final String oauthId, final Nickname nickname,
                  final MemberImage image, final MemberProfile memberProfile, final MemberSkills skills) {
        this(null, email, oauthId, null, nickname, image, memberProfile, skills);
    }

    public Member(final Long id, final Email email, final String oauthId, final EncryptedPassword encryptedPassword,
                  final Nickname nickname, final MemberImage image, final MemberProfile memberProfile, MemberSkills skills) {
        this.id = id;
        this.email = email;
        this.oauthId = oauthId;
        this.encryptedPassword = encryptedPassword;
        this.nickname = nickname;
        this.image = image;
        this.memberProfile = memberProfile;
        this.skills = skills;
    }

    public boolean isPasswordMismatch(final Password password) {
        return this.encryptedPassword.isMismatch(password);
    }

    public void addSkills(final MemberSkills skills) {
        this.skills.addAll(skills);
    }

    public MemberImage getImage() {
        return image;
    }

    public MemberProfile getMemberProfile() {
        return memberProfile;
    }

    public Nickname getNickname() {
        return nickname;
    }

    public Email getEmail() {
        return email;
    }

    public MemberSkills getSkills() {return skills;}
}
