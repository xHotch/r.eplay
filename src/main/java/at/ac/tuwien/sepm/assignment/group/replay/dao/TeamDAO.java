package at.ac.tuwien.sepm.assignment.group.replay.dao;


import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.TeamPersistenceException;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.TeamDTO;

import java.util.List;

/**
 * @author Markus Kogelbauer
 */
public interface TeamDAO {
    /**
     * Creates a new Team
     * @param teamDTO the team to create
     * @throws TeamPersistenceException if something failed during persistence of the new team
     */
    void createTeam(TeamDTO teamDTO) throws TeamPersistenceException;

    /**
     * Deletes a Team
     * @param teamDTO the team to delete
     * @throws TeamPersistenceException if something failed during persistence of the team
     */
    void deleteTeam(TeamDTO teamDTO) throws TeamPersistenceException;

    /**
     * Reads all teams that are currently stored in the database
     *
     * @throws TeamPersistenceException throws persistence exception if something failed
     *                                    while reading the teams from the database.
     */
    List<TeamDTO> readTeams() throws TeamPersistenceException;

    /**
     * Read all matches where all players from the first and second team played against each other.
     *
     * @param teamDTO1 the first team
     * @param teamDTO2 the second team
     * @return A List of MatchDTOs
     * @throws TeamPersistenceException if something failed during persistence of the team
     */
    List<MatchDTO> readTeamMatches(TeamDTO teamDTO1, TeamDTO teamDTO2) throws TeamPersistenceException;
}
