package at.ac.tuwien.sepm.assignment.group.replay.service;

import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchDTO;
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
     * @return A List of MatchDTOs
     * @throws TeamServiceException if something failed during persistence of the team
     */
    List<MatchDTO> readTeamMatches(TeamDTO teamDTO1, TeamDTO teamDTO2) throws TeamServiceException;
}
