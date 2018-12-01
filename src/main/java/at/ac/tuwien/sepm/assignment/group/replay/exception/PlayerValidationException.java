package at.ac.tuwien.sepm.assignment.group.replay.exception;

/**
 * @author Gabriel Aichinger
 */
public class PlayerValidationException extends Exception {
    public PlayerValidationException(String message) {super(message);}

    public PlayerValidationException(String message, Throwable cause) {super(message, cause);}
}
