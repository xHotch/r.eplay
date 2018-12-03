package at.ac.tuwien.sepm.assignment.group.replay.service;

import at.ac.tuwien.sepm.assignment.group.replay.dto.PlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.exception.PlayerServiceException;
import at.ac.tuwien.sepm.assignment.group.replay.exception.PlayerValidationException;

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
    int createPlayer(PlayerDTO playerDTO) throws PlayerValidationException, PlayerServiceException;
}
