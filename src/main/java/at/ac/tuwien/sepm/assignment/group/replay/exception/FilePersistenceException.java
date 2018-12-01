package at.ac.tuwien.sepm.assignment.group.replay.exception;

public class FilePersistenceException extends Exception {
    public FilePersistenceException(String message) {
        super(message);
    }

    public FilePersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
