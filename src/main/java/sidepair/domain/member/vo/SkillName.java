package sidepair.domain.member.vo;

import jakarta.persistence.Column;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import sidepair.domain.member.exception.MemberException;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SkillName {

    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 10;
    private static final String REGEX = "^[a-zA-Z가-힣]{1,10}$";

    @Column(name = "name", length = 15)
    private String value;

    public SkillName(final String value) {
        final String removedSpaceValue = value.replaceAll(" ", "");
        validate(removedSpaceValue);
        this.value = removedSpaceValue;
    }

    private void validate(final String value) {
        if (isNotValidLength(value) || isNotValidPattern(value)) {
            throw new MemberException("제약 조건에 맞지 않는 스킬 이름입니다.");
        }
    }

    private boolean isNotValidLength(final String value) {
        return value.length() < MIN_LENGTH || value.length() > MAX_LENGTH;
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

    private boolean isNotValidPattern(final String value) {
        return !value.matches(REGEX);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    public String getValue() {
        return value;
    }
}
