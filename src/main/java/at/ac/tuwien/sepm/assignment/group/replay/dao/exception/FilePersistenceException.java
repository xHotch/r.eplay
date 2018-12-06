package at.ac.tuwien.sepm.assignment.group.replay.dao.exception;

public class FilePersistenceException extends Exception {
    public FilePersistenceException(String message) {
        super(message);
    }

    public FilePersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
