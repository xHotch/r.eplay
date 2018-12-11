package at.ac.tuwien.sepm.assignment.group.replay.service.exception;

/**
 * @author Daniel Klampfl
 */
public class ReplayAlreadyExistsException extends Exception {
    public ReplayAlreadyExistsException(String message) {
        super(message);
    }

    public ReplayAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
