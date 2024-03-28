package sidepair.domain.feed.exception;

import sidepair.domain.exception.DomainException;

public class FeedException extends DomainException {
    public FeedException(final String message) {
        super(message);
    }
}
