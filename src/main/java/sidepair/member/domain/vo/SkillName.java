package sidepair.member.domain.vo;

import jakarta.persistence.Column;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import sidepair.member.exception.MemberException;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SkillName {

    private static final int MIN_LENGTH = 1;
    private static final int MAX_LENGTH = 10;

    @Column(name = "name", length = 15)
    private String value;

    public SkillName(final String value) {
        final String removedSpaceValue = value.replaceAll(" ", "");
        validate(removedSpaceValue);
        this.value = removedSpaceValue;
    }

    private void validate(final String name) {
        if (name.length() < MIN_LENGTH || name.length() > MAX_LENGTH) {
            throw new MemberException(
                    String.format("기술 이름은 최소 %d자부터 최대 %d자까지 가능합니다.", MIN_LENGTH, MAX_LENGTH));
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final SkillName that = (SkillName) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    public String getValue() {
        return value;
    }
}