package sidepair.service.aop;

import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import sidepair.domain.exception.DomainException;
import sidepair.domain.exception.UnexpectedDomainException;
import sidepair.service.exception.BadRequestException;
import sidepair.service.exception.ServerException;

@Aspect
@Component
public class ExceptionConvertAop {

    @AfterThrowing(pointcut = "within(@sidepair.service.aop.ExceptionConvert *)", throwing = "exception")
    public void convertException(final Throwable exception) {
        if (exception instanceof UnexpectedDomainException) {
            throw new ServerException(exception.getMessage());
        }
        if (exception instanceof DomainException) {
            throw new BadRequestException(exception.getMessage());
        }
    }
}
