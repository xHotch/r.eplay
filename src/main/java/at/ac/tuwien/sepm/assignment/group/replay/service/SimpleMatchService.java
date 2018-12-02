package at.ac.tuwien.sepm.assignment.group.replay.service;

import at.ac.tuwien.sepm.assignment.group.replay.dao.MatchDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchPlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.exception.MatchServiceException;
import at.ac.tuwien.sepm.assignment.group.replay.exception.MatchValidationException;
import at.ac.tuwien.sepm.assignment.group.replay.exception.MatchPersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;

/**
 * @author Daniel Klampfl
 */
@Component
public class SimpleMatchService implements MatchService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private MatchDAO matchDAO;

    public SimpleMatchService(MatchDAO matchDAO) {
        this.matchDAO = matchDAO;
    }

    @Override
    public void createMatch(MatchDTO matchDTO) throws MatchValidationException, MatchServiceException {
        LOG.trace("Called - createMatch");
        matchDTOValidator(matchDTO);
        try {
            matchDAO.createMatch(matchDTO);
        } catch (MatchPersistenceException e) {
            String msg = "Failed to create match";
            LOG.error(msg, e);
            throw new MatchServiceException(msg,e);
        }
    }

    private void matchDTOValidator(MatchDTO matchDTO) throws MatchValidationException {
        LOG.trace("Called - matchDTOValidator");
        String errMsg = "";

        if (matchDTO.getDateTime() == null) errMsg += "No MatchDate\n";
        if (matchDTO.getPlayerData() == null || matchDTO.getPlayerData().isEmpty()) errMsg += "No players found in match\n";
        else {
            if (matchDTO.getPlayerData().size() != matchDTO.getTeamSize() * 2) errMsg += "Team size does not equal player list\n";
            int countTeamBlue = 0;
            int countTeamRed = 0;
            for (MatchPlayerDTO player : matchDTO.getPlayerData()) {
                errMsg += matchPlayerDTOValidator(player);
                if (player.getTeam() == 0) {
                    countTeamBlue++;
                }
                if (player.getTeam() == 1) {
                    countTeamRed++;
                }
            }
            if (matchDTO.getTeamSize() != countTeamBlue || matchDTO.getTeamSize() != countTeamRed) errMsg += "Uneven teamsize\n";
        }
        if (!errMsg.equals("")) {
            throw new MatchValidationException(errMsg);
        }
    }

    private String matchPlayerDTOValidator(MatchPlayerDTO matchPlayerDTO) {
        LOG.trace("Called - matchDTOValidator");
        String errMsg = "";
        if (matchPlayerDTO.getName() == null || matchPlayerDTO.getName().equals("")) errMsg += "No Name\n";
        if (matchPlayerDTO.getTeam() < 0 || matchPlayerDTO.getTeam() > 1) errMsg += "Invalid Team number\n";
        if (matchPlayerDTO.getGoals() < 0) errMsg += "Goals negativ\n";
        if (matchPlayerDTO.getShots() < 0) errMsg += "Shots negativ\n";
        if (matchPlayerDTO.getAssists() < 0) errMsg += "Assists negativ\n";
        if (matchPlayerDTO.getSaves() < 0) errMsg += "Saves negativ\n";
        if (matchPlayerDTO.getScore() < 0) errMsg += "Score negativ\n";
        return errMsg;
    }
}
