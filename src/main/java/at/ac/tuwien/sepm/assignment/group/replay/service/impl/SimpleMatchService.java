package at.ac.tuwien.sepm.assignment.group.replay.service.impl;

import at.ac.tuwien.sepm.assignment.group.replay.dao.FolderDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.MatchDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.FilePersistenceException;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchPlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.MatchAlreadyExistsException;
import at.ac.tuwien.sepm.assignment.group.replay.dto.TeamSide;
import at.ac.tuwien.sepm.assignment.group.replay.dto.VideoDTO;
import at.ac.tuwien.sepm.assignment.group.replay.service.JsonParseService;
import at.ac.tuwien.sepm.assignment.group.replay.service.ReplayService;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.FileServiceException;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.MatchServiceException;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.MatchValidationException;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.MatchPersistenceException;
import at.ac.tuwien.sepm.assignment.group.replay.service.MatchService;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.ReplayAlreadyExistsException;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.List;

/**
 * @author Daniel Klampfl
 */
@Component
public class SimpleMatchService implements MatchService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private MatchDAO matchDAO;
    private FolderDAO folderDAO;


    public SimpleMatchService(MatchDAO matchDAO, FolderDAO folderDAO) {
        this.matchDAO = matchDAO;
        this.folderDAO = folderDAO;

    }

    @Override
    public void createMatch(MatchDTO matchDTO) throws MatchValidationException, MatchServiceException, ReplayAlreadyExistsException {
        LOG.trace("Called - createMatch");
        matchDTOValidator(matchDTO);
        try {
            matchDAO.createMatch(matchDTO);
        } catch (MatchPersistenceException e) {
            String msg = "Failed to create match";
            throw new MatchServiceException(msg,e);
        } catch (MatchAlreadyExistsException e) {
            throw new ReplayAlreadyExistsException("Replay already exists",e);
        }
    }

    @Override
    public List<MatchDTO> getMatches() throws MatchServiceException{
        LOG.trace("Called - getMatches");
        try {
            return matchDAO.readMatches();
        } catch (MatchPersistenceException e){
            String message = "Could not get matches from DAO";
            throw new MatchServiceException(message,e);
        }
    }

    @Override
    public void deleteMatch(MatchDTO matchDTO) throws MatchServiceException {
        LOG.trace("called - deleteMatch");
        try {
            matchDAO.deleteMatch(matchDTO);
        } catch (MatchPersistenceException e) {
            throw new MatchServiceException("could not delete match",e);
        }
    }

    @Override
    public void deleteFile(File file) throws FileServiceException {
        LOG.trace("Called - deleteFile");
        String extension = FilenameUtils.getExtension(file.getName());
        if (!extension.equals("json")){
            throw new FileServiceException("Can not delete File with type " + extension);
        }
        try {
            folderDAO.deleteFile(file);
        } catch (FilePersistenceException e){
            throw new FileServiceException("Could not delete File", e);
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
                if (player.getTeam() == TeamSide.RED) {
                    countTeamRed++;
                }
                if (player.getTeam() == TeamSide.BLUE) {
                    countTeamBlue++;
                }
            }
            if (matchDTO.getTeamSize() != countTeamBlue || matchDTO.getTeamSize() != countTeamRed) errMsg += "Uneven teamsize\n";
        }
        if (!errMsg.equals("")) {
            throw new MatchValidationException(errMsg);
        }
    }

    private String matchPlayerDTOValidator(MatchPlayerDTO matchPlayerDTO) {
        LOG.trace("Called - matchPlayerDTOValidator");
        String errMsg = "";
        if (matchPlayerDTO.getName() == null || matchPlayerDTO.getName().equals("")) errMsg += "No Name\n";
        if (matchPlayerDTO.getGoals() < 0) errMsg += "Goals negativ\n";
        if (matchPlayerDTO.getShots() < 0) errMsg += "Shots negativ\n";
        if (matchPlayerDTO.getAssists() < 0) errMsg += "Assists negativ\n";
        if (matchPlayerDTO.getSaves() < 0) errMsg += "Saves negativ\n";
        if (matchPlayerDTO.getScore() < 0) errMsg += "Score negativ\n";
        return errMsg;
    }
}
