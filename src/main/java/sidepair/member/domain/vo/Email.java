package sidepair.member.domain.vo;

import jakarta.persistence.Column;
import java.util.Objects;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import sidepair.member.exception.MemberException;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Email {

    private static final String EMAIL_REGEX = "^([\\w\\.\\_\\-])*[a-zA-Z0-9]+([\\w\\.\\_\\-])*([a-zA-Z0-9])+([\\w\\.\\_\\-])+@([a-zA-Z0-9]+\\.)+[a-zA-Z0-9]{2,8}$";

    @Column(name = "email", nullable = false, unique = true)
    private String value;

    public Email(final String value) {
        validate(value);
        this.value = value;
    }

    private void validate(final String value) {
        if (Objects.isNull(value)) {
            throw new NullPointerException("이메일은 null일 수 없습니다.");
        }

        if (isNotMatchEmailForm(value)) {
            throw new MemberException("제약 조건에 맞지 않는 이메일입니다.");
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
        final Email that = (Email) o;
        return Objects.equals(value, that.value);
    }

    private boolean isNotMatchEmailForm(final String value) {
        return !Pattern.matches(EMAIL_REGEX, value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
