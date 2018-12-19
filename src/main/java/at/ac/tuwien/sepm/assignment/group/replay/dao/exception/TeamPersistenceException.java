package at.ac.tuwien.sepm.assignment.group.replay.dao.exception;

/**
 * @author Markus Kogelbauer
 */
public class TeamPersistenceException extends Exception {
    public TeamPersistenceException(String message) {super(message);}

    public TeamPersistenceException(String message, Throwable cause) {super(message, cause);}
}
