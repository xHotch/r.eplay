package at.ac.tuwien.sepm.assignment.group.replay.exception;

/**
 * @author Gabriel Aichinger
 */
public class PlayerServiceException extends Exception {
    public PlayerServiceException(String message) {super(message);}

    public PlayerServiceException(String message, Throwable cause) {super(message, cause);}
}
