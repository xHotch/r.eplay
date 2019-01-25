package at.ac.tuwien.sepm.assignment.group.replay.service;

import at.ac.tuwien.sepm.assignment.group.replay.dto.AvgStatsDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchType;
import at.ac.tuwien.sepm.assignment.group.replay.dto.PlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.PlayerPersistenceException;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.PlayerServiceException;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.PlayerValidationException;

import java.util.List;

/**
 * Player Service for validating, calculating of statistics, creating and reading for a player
 *
 * @author Gabriel Aichinger
 */
public interface PlayerService {

    /**
     * Validates and creates the player in the database.
     *
     * @param playerDTO the player to validate and create.
     * @return created id or the id from the existing player with the platformid
     * @throws PlayerValidationException if there is invalid data in the player.
     * @throws PlayerServiceException    if the dao method throws an error.
     */
    long createPlayer(PlayerDTO playerDTO) throws PlayerValidationException, PlayerServiceException;

    /**
     * Validates list of players to be deleted and deletes all players of the list that the method is called with
     *
     * @param players players to delete
     * @throws PlayerValidationException if the list of players to delete is empty
     * @throws PlayerServiceException throws persistence exception if something failed
     *                                    while deleting a player in the database
     */
    void deletePlayers(List<PlayerDTO> players) throws PlayerValidationException, PlayerServiceException;

    /**
     * Retrieves all players of the database that should be shown in the player list
     *
     * @return list of all players in the database
     * @throws PlayerServiceException throws persistence exception if something failed
     *                                while reading the players in the database
     */
    List<PlayerDTO> getPlayers() throws PlayerServiceException;

    /**
     * adds player to the list of shown players in the player tab
     *
     * @param player player to add
     * @throws PlayerServiceException throws persistence exception if something failed
     *                                while updating the player database entry
     */
    void showPlayer(PlayerDTO player) throws PlayerServiceException;

    /**
     * Gets the average statistics from a player for a specific MatchType
     * @param playerDTO requested player for the statistics
     * @param matchType requested matchType
     * @return the average statistic
     * @throws PlayerServiceException if error occurs during persistence
     */
    AvgStatsDTO getAvgStats(PlayerDTO playerDTO, MatchType matchType) throws PlayerServiceException;
}
