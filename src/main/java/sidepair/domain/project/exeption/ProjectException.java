package sidepair.domain.project.exeption;

import sidepair.domain.exception.DomainException;

public class ProjectException extends DomainException {
    public ProjectException(final String message) {
        super(message);
    }
}
