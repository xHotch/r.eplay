package at.ac.tuwien.sepm.assignment.group.replay.dao;


import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.TeamPersistenceException;
import at.ac.tuwien.sepm.assignment.group.replay.dto.TeamDTO;

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
}
