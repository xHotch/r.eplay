package at.ac.tuwien.sepm.assignment.group.replay.dao;

import at.ac.tuwien.sepm.assignment.group.replay.dto.BoostPadDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.MatchAlreadyExistsException;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.MatchPersistenceException;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchPlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchType;
import at.ac.tuwien.sepm.assignment.group.replay.dto.PlayerDTO;

import java.time.LocalDateTime;
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

    /**
     * Deletes a match
     * @param matchDTO the match which should be deleted
     * @throws MatchPersistenceException throws persistence exception if something failed while deleting the match
     */
    void deleteMatch(MatchDTO matchDTO) throws MatchPersistenceException;

    /**
     * Reads filtered matches
     * @param name part of the name of a player or null if it should be ignored
     * @param begin start point for search or null if it should be ignored
     * @param end end point for search or null if it should be ignored
     * @param teamSize 1, 2, 3 or 0 if it should be ignored
     * @return list with the found matches
     * @throws MatchPersistenceException throws persistence exception if something failed
     * while reading the matches from the database.
     */
    List<MatchDTO> searchMatches(String name, LocalDateTime begin, LocalDateTime end, int teamSize) throws MatchPersistenceException;

    /**
     * Reads all matches from a player that are currently stored in the database
     * @throws MatchPersistenceException throws persistence exception if something failed
     * while reading the matches from the database.
        */
    List<MatchDTO> readMatchesFromPlayer(PlayerDTO playerDTO) throws MatchPersistenceException;
}
