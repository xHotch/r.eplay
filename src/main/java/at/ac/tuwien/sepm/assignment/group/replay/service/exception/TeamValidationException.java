package at.ac.tuwien.sepm.assignment.group.replay.service.exception;

/**
 * @author Markus Kogelbauer
 */
public class TeamValidationException extends Exception {
    public TeamValidationException(String message) {
        super(message);
    }

    public TeamValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
