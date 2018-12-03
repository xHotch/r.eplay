package at.ac.tuwien.sepm.assignment.group.replay.dao;

import at.ac.tuwien.sepm.assignment.group.replay.dto.PlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.exception.PlayerPersistenceException;

import java.util.List;

/**
 * @author Gabriel Aichinger
 */
public interface PlayerDAO {

    /**
     * Creates a new player in the storage for playerDao
     *
     * @param playerDTO the player data to store
     * @throws PlayerPersistenceException throws persistence exception if something failed
     *                                    while storing a player into the database.
     * @return the generated id of the player
     */
    int createPlayer(PlayerDTO playerDTO) throws PlayerPersistenceException;

    /**
     * Reads all players that are currently stored in the database
     *
     * @throws PlayerPersistenceException throws persistence exception if something failed
     *                                    while reading the players from the database.
     */
    List<PlayerDTO> readPlayers() throws PlayerPersistenceException;

    /**
     * Deletes player entry in the database
     *
     * @param playerToDelete player to delete
     */
    void deletePlayer(PlayerDTO playerToDelete) throws PlayerPersistenceException;
}
