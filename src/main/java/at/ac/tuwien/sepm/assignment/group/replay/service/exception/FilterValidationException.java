package at.ac.tuwien.sepm.assignment.group.replay.service.exception;

public class FilterValidationException extends Exception {
    public FilterValidationException(String message) {
        super(message);
    }

    public FilterValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
