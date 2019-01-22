package at.ac.tuwien.sepm.assignment.group.replay.service;

import at.ac.tuwien.sepm.assignment.group.replay.dto.PlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.PlayerTeamsDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.TeamCompareDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.TeamDTO;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.TeamServiceException;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.TeamValidationException;

import java.util.List;

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
     * Read all matches where all players from first and second team played against each other.
     *
     * @param teamDTO1 the first team
     * @param teamDTO2 the second team
     * @return a TeamCompareDTO with a MatchDTO  and MatchstatsDTO list
     * @throws TeamServiceException if something failed during persistence of the team
     */
    TeamCompareDTO readTeamMatches(TeamDTO teamDTO1, TeamDTO teamDTO2) throws TeamServiceException;

    /**
     * Reads the teams from a player
     * @param playerDTO the player
     * @return a dto with the playerDTO and a list of the TeamDTOs
     * @throws TeamServiceException if something fails during persistence
     */
    PlayerTeamsDTO readPlayerTeams(PlayerDTO playerDTO) throws TeamServiceException;
}
