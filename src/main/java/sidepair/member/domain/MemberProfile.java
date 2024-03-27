package sidepair.member.domain;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import sidepair.global.domain.BaseUpdatedTimeEntity;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberProfile extends BaseUpdatedTimeEntity {
    @Enumerated(value = EnumType.STRING)
    @Column(length = 10, nullable = false)
    private Position position;

    public MemberProfile(final Position position) {
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }
}
