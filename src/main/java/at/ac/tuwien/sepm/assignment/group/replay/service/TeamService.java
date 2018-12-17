package at.ac.tuwien.sepm.assignment.group.replay.service;

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
     * reads all Teams
     * @return list with all teams
     * @throws TeamServiceException if there occurs an error during reading
     */
    List<TeamDTO> readTeams() throws TeamServiceException;
}
