package sidepair.backend.member.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import sidepair.backend.global.domain.BaseEntity;
import sidepair.backend.member.domain.vo.Email;
import sidepair.backend.member.domain.vo.Nickname;
import sidepair.backend.member.domain.vo.ProfileImgUrl;

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

}
