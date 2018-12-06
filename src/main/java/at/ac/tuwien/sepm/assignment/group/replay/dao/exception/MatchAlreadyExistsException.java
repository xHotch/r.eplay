package at.ac.tuwien.sepm.assignment.group.replay.dao.exception;

public class MatchAlreadyExistsException extends Exception {
    public MatchAlreadyExistsException(String message) {
        super(message);
    }

    public MatchAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
