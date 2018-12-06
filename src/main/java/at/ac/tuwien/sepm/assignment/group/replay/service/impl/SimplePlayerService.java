package at.ac.tuwien.sepm.assignment.group.replay.service.impl;

import at.ac.tuwien.sepm.assignment.group.replay.dao.PlayerDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.PlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.PlayerPersistenceException;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.PlayerServiceException;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.PlayerValidationException;
import at.ac.tuwien.sepm.assignment.group.replay.service.PlayerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.List;

/**
 * @author Gabriel Aichinger
 */
@Component
public class SimplePlayerService implements PlayerService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private PlayerDAO playerDAO;

    public SimplePlayerService(PlayerDAO playerDAO) {
        this.playerDAO = playerDAO;
    }

    @Override
    public long createPlayer(PlayerDTO playerDTO) throws PlayerValidationException, PlayerServiceException {
        LOG.trace("Called - createPlayer");
        playerDTOValidator(playerDTO);
        try {
            return playerDAO.createPlayer(playerDTO);
        } catch (PlayerPersistenceException e) {
            String msg = "Failed to create player";
            LOG.error(msg, e);
            throw new PlayerServiceException(msg, e);
        }
    }

    @Override
    public void deletePlayers(List<PlayerDTO> playersToDelete) throws PlayerValidationException, PlayerServiceException {
        LOG.trace("Called - deletePlayer");
        if (playersToDelete == null || playersToDelete.isEmpty()) {
            throw new PlayerValidationException("productsToDelete is null or empty");
        }
        for (PlayerDTO p : playersToDelete) {
            try {
                playerDAO.deletePlayer(p);
            } catch (PlayerPersistenceException e) {
                String msg = "Failed to delete player";
                LOG.error(msg, e);
                throw new PlayerServiceException(msg, e);
            }

        }

    }

    @Override
    public List<PlayerDTO> getPlayers() throws PlayerServiceException {
        LOG.trace("Called - getPlayers");
        List<PlayerDTO> result;
        try {
            result = playerDAO.readPlayers();
        } catch (PlayerPersistenceException e) {
            String msg = "Failed to get players";
            LOG.error(msg, e);
            throw new PlayerServiceException(msg, e);
        }
        return result;
    }

    @Override
    public void showPlayer(PlayerDTO player) throws PlayerServiceException {
        LOG.trace("Called - showPlayer");
        try {
            playerDAO.showPlayer(player);
        } catch (PlayerPersistenceException e) {
            String msg = "Failed to add player";
            LOG.error(msg, e);
            throw new PlayerServiceException(msg, e);        }
    }

    private void playerDTOValidator(PlayerDTO playerDTO) throws PlayerValidationException {
        LOG.trace("Called - playerDTOValidator");
        String errMsg = "";
        if (playerDTO.getName() == null || playerDTO.getName().equals("")) {
            errMsg += "No name\n";
        }
        if (playerDTO.getPlatformID() <= 0) {
            errMsg += "plattformid is smaller or equal to zero\n";
        }
        if (!errMsg.equals("")) {
            throw new PlayerValidationException(errMsg);
        }
    }
}
