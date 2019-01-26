package at.ac.tuwien.sepm.assignment.group.replay.service;

import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.FilePersistenceException;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.MatchAlreadyExistsException;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchStatsDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.TeamSide;
import at.ac.tuwien.sepm.assignment.group.replay.dto.VideoDTO;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.*;

import java.io.File;
import java.time.LocalDateTime;
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
     * @throws ReplayAlreadyExistsException if the match already exists
     */
    void createMatch(MatchDTO matchDTO) throws MatchValidationException, MatchServiceException, ReplayAlreadyExistsException;


    List<MatchDTO> getMatches() throws MatchServiceException;

    /**
     * Deletes a match
     * @param matchDTO the match to delete
     * @throws MatchServiceException if the dao method throws an error
     */
    void deleteMatch(MatchDTO matchDTO) throws MatchServiceException;

    /**
     * Calls DAO to delete a file.
     * file has to be .json
     * @param file the file to delete
     * @throws FileServiceException if the file could not be deleted
     */
    void deleteFile(File file) throws FileServiceException;

    /**
     * Reads filtered matches
     * @param name part of the name of a player or null if it should be ignored
     * @param begin start point for search or null if it should be ignored
     * @param end end point for search or null if it should be ignored
     * @param teamSize 1, 2, 3 or 0 if it should be ignored
     * @return list with the found matches
     * @throws MatchServiceException if an error occurs during persistence
     * @throws FilterValidationException if the parameters are invalid
     */
    List<MatchDTO> searchMatches(String name, LocalDateTime begin, LocalDateTime end, int teamSize) throws MatchServiceException, FilterValidationException;

    /**
     * calculates the total stats from a team in a match
     * @param matchDTO the match
     * @param teamSide blue or red side
     * @return the teamStats from a team
     */
    MatchStatsDTO calcTeamStats(MatchDTO matchDTO, TeamSide teamSide);

    /**
     * Gets the Heatmap Images from the heatmaps directory and saves it to the MatchDTO
     * @param matchDTO the match to get the heatmap
     * @throws FileServiceException if the heatmap image could not be read
     */
    void getHeatmaps(MatchDTO matchDTO) throws FileServiceException;
}
