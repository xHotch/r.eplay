package at.ac.tuwien.sepm.assignment.group.replay.service.exception;

/**
 * @author Markus Kogelbauer
 */
public class TeamServiceException extends Exception {
    public TeamServiceException(String message) {
        super(message);
    }

    public TeamServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
