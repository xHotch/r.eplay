package at.ac.tuwien.sepm.assignment.group.replay.service;

import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchStatsDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.TeamDTO;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.TeamServiceException;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.TeamValidationException;

import java.util.List;
import java.util.Map;

/**
 * Team Service for validating, creating and reading for a team
 *
 * @author Markus Kogelbauer
 */
public interface TeamService {

    /**
     * Validates and creates a team
     * @param teamDTO the team which should be created
     * @throws TeamValidationException if there is invalid data in the team
     * @throws TeamServiceException if there occurs an error during persistence
     */
    void createTeam(TeamDTO teamDTO) throws TeamValidationException, TeamServiceException;

    /**
     * Validates and deletes a team
     * @param teamDTO the team which should be deleted
     * @throws TeamValidationException if there is invalid data in the team
     * @throws TeamServiceException if there occurs an error during persistence
     */
    void deleteTeam(TeamDTO teamDTO) throws TeamValidationException, TeamServiceException;

    /**
     * reads all Teams
     * @return list with all teams
     * @throws TeamServiceException if there occurs an error during reading
     */
    List<TeamDTO> readTeams() throws TeamServiceException;

    /**
     * Read the stats from all matches where all players from the team played
     *
     * @param teamDTO the team to get the stats from
     * @return A Map with the matchid as the key and a list of MatchStats
     * @throws TeamServiceException if something failed during persistence of the team
     */
    Map<Integer, List<MatchStatsDTO>> readTeamStats(TeamDTO teamDTO) throws TeamServiceException;
}
