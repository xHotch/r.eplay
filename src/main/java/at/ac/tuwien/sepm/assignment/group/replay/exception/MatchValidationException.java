package at.ac.tuwien.sepm.assignment.group.replay.exception;

/**
 * @author Daniel Klampfl
 */
public class MatchValidationException extends Exception {
    public MatchValidationException(String message) {
        super(message);
    }

    public MatchValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
