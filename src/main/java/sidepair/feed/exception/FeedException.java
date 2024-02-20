package sidepair.feed.exception;

import sidepair.global.domain.exception.DomainException;

public class FeedException extends DomainException {

    public FeedException(final String message) {
        super(message);
    }
}
