package at.ac.tuwien.sepm.assignment.group.replay.exception;

/**
 * @author Daniel Klampfl
 */
public class MatchPersistenceException extends Exception {
    public MatchPersistenceException(String message) {
        super(message);
    }
    public MatchPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
