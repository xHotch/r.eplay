package at.ac.tuwien.sepm.assignment.group.replay.service.exception;

/**
 * @author Daniel Klampfl
 */
public class MatchServiceException extends Exception{
    public MatchServiceException(String message) {
        super(message);
    }

    public MatchServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
