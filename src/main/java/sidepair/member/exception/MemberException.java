package sidepair.member.exception;

import sidepair.member.domain.vo.Email;

public class MemberException extends RuntimeException {

    public MemberException(final String message) {
        super(message);
    }

    public static class NickNameLengthException extends MemberException {

        public NickNameLengthException(final int allowedLength, final String inputNickName) {
            super(String.format(
                    "닉네임의 길이가 최대 길이를 초과했습니다. - request info { allowed_length : %d, input_value_length : %d }",
                    allowedLength,
                    inputNickName.length())
            );
        }
    }

    public static class NickNameBlankException extends MemberException {

        public NickNameBlankException() {
            super("닉네임은 공백을 제외한 1자 이상이어야합니다.");
        }
    }

    public static class EmailRegexException extends MemberException {
        public EmailRegexException(final String inputEmail) {
            super(String.format("정해진 이메일의 양식이 아닙니다. - request info { email : %s }", inputEmail));
        }
    }

    public static class DuplicateEmailException extends MemberException {

        public DuplicateEmailException(final Email inputEmail) {
            super(String.format("이미 존재하는 이메일입니다. - request info { email : %s }", inputEmail));
        }
    }

    public static class PasswordRegexException extends MemberException {
        public PasswordRegexException(final String inputPassword) {
            super(String.format("정해진 비밀번호의 양식이 아닙니다. - request info { password : %s }", inputPassword));
        }
    }

    public static class MemberNotFoundException extends MemberException {
        public MemberNotFoundException(final String email) {
            super(String.format("조회한 멤버가 존재하지 않습니다. - request info { email : %s }", email));
        }
    }

    public static class SkillCountExceededException extends MemberException {

        public SkillCountExceededException(final int maxAllowedCount) {
            super(String.format(
                    "최대 스킬 개수를 초과했습니다. - 최대 허용 스킬 개수: %d",
                    maxAllowedCount)
            );
        }
    }

    public static class DuplicateSkillException extends MemberException {

        public DuplicateSkillException(final String skillName) {
            super(String.format("이미 추가된 스킬입니다. - 중복된 스킬: %s", skillName));
        }
    }
}
