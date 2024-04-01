package sidepair.domain.member.exception;

import sidepair.domain.exception.DomainException;

public class MemberException extends DomainException {

    public MemberException(final String message) {
        super(message);
    }
}
