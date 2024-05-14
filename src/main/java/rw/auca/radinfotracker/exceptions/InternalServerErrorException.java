package rw.auca.radinfotracker.exceptions;


import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

public class InternalServerErrorException extends AbstractThrowableProblem {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(InternalServerErrorException.class);

    public InternalServerErrorException(String message) {
        super(null, message, Status.INTERNAL_SERVER_ERROR);
        logger.error("Internal server error: {}", message);
    }
}


