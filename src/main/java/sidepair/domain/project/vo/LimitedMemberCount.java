package sidepair.domain.project.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import sidepair.domain.project.exeption.ProjectException;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LimitedMemberCount {

    private static final int MIN = 1;
    private static final int MAX = 6;

    @Column(name = "limited_member_count")
    private int value;

    public LimitedMemberCount(final int value) {
        validate(value);
        this.value = value;
    }

    private void validate(final int value) {
        if (value < MIN || value > MAX) {
            throw new ProjectException("제한 인원 수가 적절하지 않습니다.");
        }
    }

    public int getValue() {
        return value;
    }
}
