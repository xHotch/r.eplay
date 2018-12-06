package at.ac.tuwien.sepm.assignment.group.replay.dao;

import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.MatchAlreadyExistsException;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.MatchPersistenceException;

import java.util.List;

/**
 *
 */
public interface MatchDAO {

    /**
     * Creates a new match in the storage from matchDao
     * @param matchDTO the match data to store
     * @throws MatchAlreadyExistsException if the same match already exists in the database
     * @throws MatchPersistenceException throws persistence exception if something failed
     * while writing to the database
     */
    void createMatch(MatchDTO matchDTO) throws MatchPersistenceException, MatchAlreadyExistsException;

    /**
     * Reads all matches that are currently stored in the database
     * @throws MatchPersistenceException throws persistence exception if something failed
     * while reading the matches from the database.
     */
    List<MatchDTO> readMatches() throws MatchPersistenceException;
}
