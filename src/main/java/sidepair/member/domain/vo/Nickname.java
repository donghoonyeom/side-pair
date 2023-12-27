package sidepair.member.domain.vo;

import jakarta.persistence.Column;
import java.util.Objects;
import sidepair.member.exception.MemberException;

public class Nickname {

    public static final int MAX_LENGTH = 8;

    @Column(name = "nickname", nullable = false, length = MAX_LENGTH)
    private String value;

    public Nickname(final String value) {
        validateNull(value);
        final String trimmedValue = value.trim();
        validate(trimmedValue);
        this.value = trimmedValue;
    }

    private void validateNull(final String value) {
        if (Objects.isNull(value)) {
            throw new NullPointerException("닉네임은 null일 수 없습니다.");
        }
    }

    private void validate(final String value) {
        if (value.length() > MAX_LENGTH) {
            throw new MemberException.NameLengthException(MAX_LENGTH, value);
        }
        if (value.isBlank()) {
            throw new MemberException.NickNameBlankException();
        }
    }

    public Nickname change(final String nickname) {
        return new Nickname(nickname);
    }


}
