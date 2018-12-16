package at.ac.tuwien.sepm.assignment.group.replay.service;

import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.MatchAlreadyExistsException;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.MatchServiceException;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.MatchValidationException;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.ReplayAlreadyExistsException;

import java.util.List;

/**
 * Match Service for validating, calculating of statistics, creating and reading for a match
 *
 * @author Daniel Klampfl
 */
public interface MatchService {

    /**
     * Validates and creates the match in the database.
     *
     * @param matchDTO the match to validate and create.
     * @throws MatchValidationException if there is invalid data in the match.
     * @throws MatchServiceException    if the dao method throws an error.
     * @throws MatchAlreadyExistsException if the match already exists
     */
    void createMatch(MatchDTO matchDTO) throws MatchValidationException, MatchServiceException, ReplayAlreadyExistsException;

    List<MatchDTO> getMatches() throws MatchServiceException;

    /**
     * Deletes a match
     * @param matchDTO the match to delete
     * @throws MatchServiceException if the dao method throws an error
     */
    void deleteMatch(MatchDTO matchDTO) throws MatchServiceException;
}
