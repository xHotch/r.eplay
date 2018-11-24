package at.ac.tuwien.sepm.assignment.group.replay.dao;

import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchDTO;
import at.ac.tuwien.sepm.assignment.group.replay.exception.PersistenceException;

/**
 *
 */
public interface MatchDAO {

    /**
     * Creates a new match in the storage from matchDao
     * @param matchDTO the match data to store
     */
    void createMatch(MatchDTO matchDTO) throws PersistenceException;
}
