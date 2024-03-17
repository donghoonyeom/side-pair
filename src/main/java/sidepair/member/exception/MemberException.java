package sidepair.member.exception;

import sidepair.global.domain.exception.DomainException;

public class MemberException extends DomainException {

    public MemberException(final String message) {
        super(message);
    }
}
