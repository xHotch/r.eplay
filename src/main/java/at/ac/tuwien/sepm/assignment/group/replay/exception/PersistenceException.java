package at.ac.tuwien.sepm.assignment.group.replay.exception;

/**
 * @author Daniel Klampfl
 */
public class PersistenceException extends Exception {
    public PersistenceException(String message) {
        super(message);
    }
    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
