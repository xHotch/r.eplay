package at.ac.tuwien.sepm.assignment.group.replay.service.impl;

import at.ac.tuwien.sepm.assignment.group.replay.dao.MatchDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.PlayerDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.MatchPersistenceException;
import at.ac.tuwien.sepm.assignment.group.replay.dto.*;
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
    private MatchDAO matchDAO;

    public SimplePlayerService(PlayerDAO playerDAO, MatchDAO matchDAO) {
        this.playerDAO = playerDAO;
        this.matchDAO = matchDAO;
    }

    @Override
    public long createPlayer(PlayerDTO playerDTO) throws PlayerValidationException, PlayerServiceException {
        LOG.trace("Called - createPlayer");
        playerDTOValidator(playerDTO);
        try {
            return playerDAO.createPlayer(playerDTO);
        } catch (PlayerPersistenceException e) {
            String msg = "Failed to create player";
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
            throw new PlayerServiceException(msg, e);        }
    }

    @Override
    public AvgStatsDTO getAvgStats(PlayerDTO playerDTO, MatchType matchType) throws PlayerServiceException {
        LOG.trace("Called - getAvgStats");
        try {
            AvgStatsDTO avgStatsDTO = playerDAO.getAvgStats(playerDTO, matchType);
            List<MatchDTO> matches = matchDAO.readMatchesFromPlayer(playerDTO);
            int wins = 0;
            int losses = 0;
            int teamSize = 3;
            if (matchType == MatchType.RANKED1V1) {
                teamSize = 1;
            } else if(matchType == MatchType.RANKED2V2) {
                teamSize = 2;
            }
            for (MatchDTO matchDTO : matches) {
                if (matchDTO.getTeamSize() == teamSize) {
                    if (won(matchDTO, playerDTO)) {
                        wins++;
                    } else {
                        losses++;
                    }
                }
            }
            avgStatsDTO.setWins(wins);
            avgStatsDTO.setLosses(losses);
            return avgStatsDTO;
        } catch (PlayerPersistenceException | MatchPersistenceException e) {
            throw new PlayerServiceException("failed to get stats", e);
        }
    }

    private boolean won(MatchDTO matchDTO, PlayerDTO playerDTO) {
        int teamBlueGoals = 0;
        int teamRedGoals = 0;
        TeamSide myTeam = null;
        for (MatchPlayerDTO matchPlayerDTO : matchDTO.getPlayerData()) {
            if (matchPlayerDTO.getPlayerDTO().getId() == playerDTO.getId()) {
                myTeam = matchPlayerDTO.getTeam();
            }
            if (matchPlayerDTO.getTeam() == TeamSide.BLUE) {
                teamBlueGoals += matchPlayerDTO.getGoals();
            } else {
                teamRedGoals += matchPlayerDTO.getGoals();
            }
        }
        if (myTeam == TeamSide.BLUE) {

            return teamBlueGoals > teamRedGoals;

        } else {
            return teamBlueGoals < teamRedGoals;
        }
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
