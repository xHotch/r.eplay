package at.ac.tuwien.sepm.assignment.group.replay.service;

import at.ac.tuwien.sepm.assignment.group.replay.dao.PlayerDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.PlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.exception.PlayerPersistenceException;
import at.ac.tuwien.sepm.assignment.group.replay.exception.PlayerServiceException;
import at.ac.tuwien.sepm.assignment.group.replay.exception.PlayerValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;

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
    public int createPlayer(PlayerDTO playerDTO) throws PlayerValidationException, PlayerServiceException {
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

    private void playerDTOValidator(PlayerDTO playerDTO) throws PlayerValidationException {
        LOG.trace("Called - playerDTOValidator");
        String errMsg = "";
        if (playerDTO.getName() == null || playerDTO.getName().equals("")) {
            errMsg += "No name\n";
        }
        if (playerDTO.getPlattformid() <= 0) {
            errMsg += "plattformid is smaller or equal to zero\n";
        }
        if (!errMsg.equals("")) {
            throw new PlayerValidationException(errMsg);
        }
    }
}