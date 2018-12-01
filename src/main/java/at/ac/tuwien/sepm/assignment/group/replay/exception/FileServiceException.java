package at.ac.tuwien.sepm.assignment.group.replay.exception;

/**
 * @author Philipp Hochhauser
 */
public class FileServiceException extends Exception {

    public FileServiceException(String message) {
        super(message);
    }

    public FileServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}
